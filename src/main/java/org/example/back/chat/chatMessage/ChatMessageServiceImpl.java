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
import org.example.back.chat.exception.ChatMessageNotFoundException;
import org.example.back.chat.exception.ChatRoomNotFoundException;
import org.example.back.chat.exception.ChatRoomNotValidException;
import org.example.back.chat.exception.ChatRoomParticipantsNotFoundException;
import org.example.back.chat.util.RedisChatUtil;
import org.example.back.chat.util.StompHeaderAccessorUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

	private static final String ROUTING_KEY_PREFIX = "/exchange/chat.exchange/room.";


	// 초기 연결 처리 - 토큰 검증 및 기본 설정만 수행
	@Override
	public void handleInitialConnect(StompHeaderAccessor accessor) {
		Long profileId = stompHeaderAccessorUtil.getMemberIdInSession(accessor);
		// 필요한 경우 추가적인 초기화 작업 수행
	}


	@Override
	public ChatMessageRes sendMessage(StompHeaderAccessor accessor, ChatDto.ChatMessageReq req) {
		Long senderId = stompHeaderAccessorUtil.getMemberIdInSession(accessor);
		Long chatRoomId = stompHeaderAccessorUtil.getChatRoomIdInSession(accessor);

		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));

		// 1대1 채팅 검증
		if (chatRoom.getParticipantCount() != 2) {
			throw new ChatRoomNotValidException("잘못된 채팅방 구성입니다.");
		}
		// 채팅 메시지 생성 및 저장
		ChatMessage chatMessage = req.createChatMessage(chatRoom.getId(), senderId);
		chatMessage.setMessageType(req.getMessageType());
		ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

		// 수신자 찾기 (발신자가 아닌 참가자)
		Long receiverId = chatRoom.getParticipants().stream()
			.map(ChatRoomParticipant::getProfileId)
			.filter(id -> !id.equals(senderId))
			.findFirst()
			.orElseThrow(() -> new ChatRoomParticipantsNotFoundException("수신자를 찾을 수 없습니다."));

		// 수신자가 온라인인지 확인
		Set<Long> onlineMembers = redisChatUtil.getOnlineMembers(chatRoomId);
		int unreadCount = onlineMembers.contains(receiverId) ? 0 : 1;

		MessageRes messageRes = ChatMessageRes.createRes(savedMessage, unreadCount);
		ChatMessageRes messageResponse = (ChatMessageRes) messageRes;

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 직렬화를 위해
			String jsonMessage = objectMapper.writeValueAsString(messageResponse);
			System.out.println("Sending message format: " + jsonMessage);
		} catch (JsonProcessingException e) {
			System.err.println("Error printing message format: " + e.getMessage());
		}

		System.out.println("전송하려는 메세지의 채팅방 id는 " + messageResponse.getChatRoomId() +"입니다");

		String destination = ROUTING_KEY_PREFIX + chatRoomId;
		messagingTemplate.convertAndSend(destination, messageResponse);

		return messageResponse;
	}

	@Transactional(readOnly = true)
	@Override
	public List<MessageRes> getChatMessages(Long chatRoomId, int page, int size) {
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new ChatRoomParticipantsNotFoundException("채팅방을 찾을 수 없습니다."));

		Set<Long> onlineMembersInChatRoom = redisChatUtil.getOnlineMembers(chatRoomId);

		// 페이징 처리
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(
			chatRoom.getId(),
			pageable
		);

		List<MessageRes> messageResList = chatMessages.getContent().stream()
			.map(chatMessage -> {
				int unreadCnt = chatRoom.getUnreadCount(onlineMembersInChatRoom, chatMessage.getCreatedAt());
				return ChatMessageRes.createRes(chatMessage, unreadCnt);
			})
			.toList();

		return messageResList;
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
			.orElseThrow(() -> new ChatRoomParticipantsNotFoundException("채팅방을 찾을 수 없습니다."));

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
			.orElseThrow(() -> new ChatMessageNotFoundException("메시지를 찾을 수 없습니다."));

		message.setMessageType(MessageType.DELETED_MESSAGE);
		chatMessageRepository.save(message);

		MessageRes deleteNotification = ChatMessageRes.createRes(message, 0);
		deleteNotification.setMessageType(MessageType.DELETED_MESSAGE);
		//여기서 deleteNotification 에 String Id 추가해서 전송
		System.out.println("삭제할 메세지는!!!!!!"+deleteNotification.getId());

		messagingTemplate.convertAndSend(
			"/exchange/chat.exchange/room." + chatRoomId,
			deleteNotification
		);
	}


}