package org.example.back.chat.chatRoom;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.back.chat.chatMessage.ChatMessage;
import org.example.back.chat.chatMessage.ChatMessageRepository;

import org.example.back.chat.chatroommember.ChatRoomParticipant;
import org.example.back.chat.chatroommember.ChatRoomParticipantRepository;
import org.example.back.chat.common.constant.MessageType;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatMessageRes;
import org.example.back.chat.common.dto.ChatRoomAccessResponse;
import org.example.back.chat.common.dto.ChatRoomParticipantsRecord;
import org.example.back.chat.common.dto.MessageRes;
import org.example.back.chat.common.dto.SimpleChatRoomRecord;
import org.example.back.chat.exception.ChatRoomAccessDeniedException;
import org.example.back.chat.exception.ChatRoomNotFoundException;
import org.example.back.chat.exception.ChatRoomNotValidException;
import org.example.back.chat.exception.ChatRoomParticipantsNotFoundException;
import org.example.back.chat.util.RedisChatUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomParticipantRepository chatRoomParticipantRepository;
	private final RabbitTemplate rabbitTemplate;
	private final RedisChatUtil redisChatUtil;
	private final SimpMessagingTemplate messagingTemplate;
	private static final String ROUTING_KEY_PREFIX = "room.";


	@Override
	public ChatDto.ChatRoomCreateRes createMatchedChatRoom(Long fromProfileId, Long toProfileId) {

		ChatRoom newRoom = ChatRoom.emptyChatRoom();
		newRoom.addParticipant(new ChatRoomParticipant(newRoom, fromProfileId, toProfileId));
		newRoom.addParticipant(new ChatRoomParticipant(newRoom, toProfileId,fromProfileId));
		chatRoomRepository.save(newRoom);

		// 초기 메시지 생성 및 전송
		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoomId(newRoom.getId())
			.profileId(fromProfileId)
			.content("매칭되었습니다. 대화를 시작해보세요!")
			.createdAt(LocalDateTime.now())
			.build();


		chatMessageRepository.save(chatMessage);
		rabbitTemplate.convertAndSend(ROUTING_KEY_PREFIX + newRoom.getId(),
			ChatMessageRes.createRes(chatMessage, newRoom.getParticipantCount()));

		return ChatDto.ChatRoomCreateRes.createRes(newRoom.getId(), fromProfileId, toProfileId);
	}


	// public List<SimpleChatRoomRecord> getSimpleChatRooms(Long profileId) {
	// 	List<ChatRoomParticipant> participants =
	// 		chatRoomParticipantRepository.findAllByProfileId(profileId);
	//
	// 	return participants.stream()
	// 		.map(participant -> {
	// 			// 읽지 않은 메시지 수 계산 (자신이 보낸 메시지 제외)
	// 			Long unreadCount = chatMessageRepository
	// 				.countByChatRoomIdAndProfileIdNotAndCreatedAtAfter(
	// 					participant.getChatRoom().getId(),
	// 					profileId,  // 자신이 보낸 메시지 제외
	// 					participant.getLastEntryTime()
	// 				);
	//
	// 			return new SimpleChatRoomRecord(
	// 				participant.getChatRoom().getId(),
	// 				profileId,
	// 				participant.getPartnerProfileId(),
	// 				unreadCount
	// 			);
	// 		})
	// 		.toList();
	// }
	// 채팅방 메시지를 읽었을 때 호출되는 메서드 추가
	@Transactional
	public void updateLastReadTime(Long chatRoomId, Long profileId) {
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));

		ChatRoomParticipant participant = chatRoom.getParticipants().stream()
			.filter(p -> p.getProfileId().equals(profileId))
			.findFirst()
			.orElseThrow(() -> new ChatRoomParticipantsNotFoundException("참가자를 찾을 수 없습니다."));

		participant.updateLastEntryTime();  // 마지막 읽은 시간 업데이트
	}

	// getSimpleChatRooms 메서드 수정
	public List<SimpleChatRoomRecord> getSimpleChatRooms(Long profileId) {
		List<ChatRoomParticipant> participants =
			chatRoomParticipantRepository.findAllByProfileId(profileId);

		return participants.stream()
			.map(participant -> {
				Long chatRoomId = participant.getChatRoom().getId();
				Set<Long> onlineMembers = redisChatUtil.getOnlineMembers(chatRoomId);

				// 자신이 온라인이면 unreadCount를 0으로 설정
				if (onlineMembers != null && onlineMembers.contains(profileId)) {
					return new SimpleChatRoomRecord(
						chatRoomId,
						profileId,
						participant.getPartnerProfileId(),
						0L
					);
				}

				// 오프라인일 경우에만 읽지 않은 메시지 수 계산
				Long unreadCount = chatMessageRepository
					.countByChatRoomIdAndProfileIdNotAndCreatedAtAfter(
						chatRoomId,
						profileId,
						participant.getLastEntryTime()
					);

				return new SimpleChatRoomRecord(
					chatRoomId,
					profileId,
					participant.getPartnerProfileId(),
					unreadCount
				);
			})
			.toList();
	}

	public ChatRoomParticipantsRecord getChatRoomParticipants(Long roomId) {
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(roomId)
			.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));

		List<Long> participantIds = chatRoom.getParticipants().stream()
			.map(ChatRoomParticipant::getProfileId)
			.toList();

		return new ChatRoomParticipantsRecord(roomId, participantIds);
	}


	@Override
	@Transactional
	public void leaveChatRoom(Long chatRoomId, Long profileId) {
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));

		// 채팅방 참가자 정보 삭제 (영속성 전이로 인해 참가자도 함께 삭제됨)
		chatRoomRepository.delete(chatRoom);

		// Redis에서 채팅방 정보 완전히 삭제
		redisChatUtil.deleteChatRoom(chatRoomId);

		// 채팅방 나가기 메시지 생성 및 전송 (채팅 기록용)
		ChatMessage leaveMessage = ChatMessage.builder()
			.chatRoomId(chatRoomId)
			.profileId(profileId)
			.content("상대방이 채팅방을 나갔습니다.")
			.messageType(MessageType.LEAVE_MESSAGE)
			.createdAt(LocalDateTime.now())
			.build();
		chatMessageRepository.save(leaveMessage);

		// 상대방에게 나가기 알림 전송
		MessageRes leaveNotification = ChatMessageRes.createRes(leaveMessage, 0);
		messagingTemplate.convertAndSend(
			"/exchange/chat.exchange/room." + chatRoomId,
			leaveNotification
		);
	}




	@Transactional(readOnly = true)
	public ChatRoomAccessResponse validateAccess(Long chatRoomId, Long profileId) {
		// 상세 검증 로직
		ChatRoom chatRoom = chatRoomRepository.findByIdWithParticipants(chatRoomId)
			.orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다."));

		if (chatRoom.getParticipantCount() != 2) {
			throw new ChatRoomNotValidException("잘못된 채팅방 구성입니다.");
		}

		ChatRoomParticipant participant = getParticipant(chatRoom, profileId);

		return new ChatRoomAccessResponse(
			chatRoom.getId(),
			participant.getPartnerProfileId(),
			true
		);
	}

	@Transactional(readOnly = true)
	public boolean isAccessibleChatRoom(Long chatRoomId, Long profileId) {
		// 간단한 존재 여부 체크 (DB 쿼리 한 번으로 해결)
		return chatRoomParticipantRepository.existsByChatRoom_IdAndProfileId(
			chatRoomId,
			profileId
		);
	}

	private ChatRoomParticipant getParticipant(ChatRoom chatRoom, Long profileId) {
		return chatRoom.getParticipants().stream()
			.filter(p -> p.getProfileId().equals(profileId))
			.findFirst()
			.orElseThrow(() -> new ChatRoomAccessDeniedException("접근 권한이 없습니다."));
	}
}