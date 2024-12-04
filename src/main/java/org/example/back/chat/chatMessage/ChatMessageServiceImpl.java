package org.example.back.chat.chatMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.example.back.chat.chatRoom.ChatRoom;
import org.example.back.chat.chatRoom.ChatRoomRepository;
import org.example.back.chat.chatroommember.ChatRoomParticipant;
import org.example.back.chat.common.constant.MessageType;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatMessageRes;
import org.example.back.chat.common.dto.ChatRoomEnterRequest;
import org.example.back.chat.common.dto.ChatSyncRequestRes;
import org.example.back.chat.common.dto.MessageRes;
import org.example.back.chat.util.RedisChatUtil;
import org.example.back.chat.util.StompHeaderAccessorUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final RedisChatUtil redisChatUtil;
	private final StompHeaderAccessorUtil stompHeaderAccessorUtil;
	private final ChatRoomRepository chatRoomRepository;

	private static final String ROUTING_KEY_PREFIX = "exchange/chat.exchange/room.";


	// 초기 연결 처리 - 토큰 검증 및 기본 설정만 수행
	@Override
	public void handleInitialConnect(StompHeaderAccessor accessor) {
		Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor);
		// 필요한 경우 추가적인 초기화 작업 수행
	}

	// 실제 채팅방 입장 처리
	@Override
	public void handleChatRoomEnter(StompHeaderAccessor accessor, ChatRoomEnterRequest request) {
		Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor);
		Long chatRoomId = request.getChatRoomId();

		// 세션에 채팅방 ID 저장
		stompHeaderAccessorUtil.setChatRoomIdInSession(accessor, chatRoomId);

		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

		enterChatRoom(chatRoom.getId(), profileId);
		readUnreadMessages(chatRoom, profileId);
	}

	@Override
	public ChatMessageRes sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq req) {
		Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor);

		Long chatRoomId = stompHeaderAccessorUtil.getChatRoomIdInSession(accessor);
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

		// 채팅 메시지 생성 및 저장
		ChatMessage chatMessage = req.createChatMessage(chatRoom.getId(), profileId);

		//원래 파일의 타입 정해놓기
		chatMessage.setMessageType(req.getMessageType());

		// 저장하고 저장된 객체를 다시 받아야 함
		ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
		System.out.println("Saved message with ID: " + savedMessage.getId());

		// 안읽은 메시지 수 계산
		int unreadCount = calculateUnreadCnt(chatRoom);

		// ChatMessageRes로 캐스팅하여 반환
		ChatMessageRes messageResponse = (ChatMessageRes) ChatMessageRes.createRes(chatMessage, unreadCount);


		System.out.println("Saved message ID: " + messageResponse.getId());


		// RabbitMQ로 메시지 전송
		String destination = "/exchange/chat.exchange/room." + chatRoomId;
		messagingTemplate.convertAndSend(destination, messageResponse);

		System.out.println("Sending to RabbitMQ - Destination: " + destination);
		System.out.println("Message: " + messageResponse);



		return messageResponse;
	}

	private int calculateUnreadCnt(ChatRoom chatRoom) {
		int onlineMemberCnt = redisChatUtil.getOnlineMemberCntInChatRoom(chatRoom.getId());
		int unreadCnt = chatRoom.getParticipantCount() - onlineMemberCnt;
		return unreadCnt;
	}

	@Override
	@Transactional
	public List<MessageRes> getChatMessages(Long chatRoomId) {
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

		Set<Long> onlineMembersInChatRoom = redisChatUtil.getOnlineMembers(chatRoomId);

		List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getId());

		List<MessageRes> messageResList = chatMessages.stream()
			.map(chatMessage -> {
				int unreadCnt = chatRoom.getUnreadCount(onlineMembersInChatRoom, chatMessage.getCreatedAt());
				return ChatMessageRes.createRes(chatMessage, unreadCnt);
			})
			.toList();

		return messageResList;
	}

	private void enterChatRoom(Long chatRoomId, Long memberId) {
		redisChatUtil.addChatRoom2Member(chatRoomId, memberId);
	}

	private void readUnreadMessages(ChatRoom chatRoom, Long profileId) {
		ChatRoomParticipant chatRoomMember = chatRoom.getParticipant(profileId);
		LocalDateTime lastEntryTime = chatRoomMember.getLastEntryTime();

		boolean existsUnreadMessage = chatMessageRepository.existsByChatRoomIdAndCreatedAtAfter(chatRoom.getId(), lastEntryTime);
		boolean existsOnlineChatRoomMember = redisChatUtil.getOnlineMemberCntInChatRoom(chatRoom.getId()) > 1;

		if (existsUnreadMessage && existsOnlineChatRoomMember)
			sendChatSyncRequestMessage(chatRoom.getId());
	}

	private void sendChatSyncRequestMessage(Long chatRoomId) {
		MessageRes messageRes = ChatSyncRequestRes.createRes();
		messagingTemplate.convertAndSend(ROUTING_KEY_PREFIX + chatRoomId, messageRes);
	}

	@Transactional
	@Override
	public void handleDisconnectMessage(StompHeaderAccessor accessor) {
		Long profileId = stompHeaderAccessorUtil.removeMemberIdInSession(accessor);

		// chatRoomId가 없으면 조기 반환
		Long chatRoomId = null;
		try {
			chatRoomId = stompHeaderAccessorUtil.removeChatRoomIdInSession(accessor);
		} catch (RuntimeException e) {
			return;
		}

		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

		ChatRoomParticipant chatRoomParticipant = chatRoom.getParticipant(profileId);
		chatRoomParticipant.updateLastEntryTime();

		exitChatRoom(chatRoom, profileId);
	}

	@Override
	public void exitChatRoom(ChatRoom chatRoom, Long profileId) {
		redisChatUtil.removeChatRoom2Member(chatRoom.getId(), profileId);
	}



	@Override
	@Transactional
	public void deleteMessage(Long chatRoomId, Long profileId, String messageId) {

		System.out.println("Attempting to find message with:");
		System.out.println("chatRoomId: " + chatRoomId);
		System.out.println("profileId: " + profileId);
		System.out.println("messageId: " + messageId);


		// ID로만 먼저 찾아보기
		Optional<ChatMessage> messageById = chatMessageRepository.findById(messageId);
		System.out.println("Message found by ID only: " + messageById.isPresent());


		ChatMessage message = chatMessageRepository
			.findByChatRoomIdAndProfileIdAndId(chatRoomId, profileId, messageId)
			.orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));

		message.setMessageType(MessageType.DELETED_MESSAGE);
		chatMessageRepository.save(message);

		MessageRes deleteNotification = ChatMessageRes.createRes(message, 0);
		deleteNotification.setMessageType(MessageType.DELETED_MESSAGE);
		//여기서 deleteNotification 에 String Id 추가해서 전송

		messagingTemplate.convertAndSend(
			"/exchange/chat.exchange/room." + chatRoomId,
			deleteNotification
		);
	}

	@Override
	@Transactional
	public void leaveChatRoom(Long chatRoomId, Long profileId) {
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

		// 채팅방 참가자 삭제
		ChatRoomParticipant participant = chatRoom.getParticipant(profileId);
		chatRoom.getParticipants().remove(participant);

		// Redis에서도 참가자 정보 삭제
		redisChatUtil.removeChatRoom2Member(chatRoomId, profileId);
	}
}