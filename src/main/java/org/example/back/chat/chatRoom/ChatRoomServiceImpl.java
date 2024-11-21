package org.example.back.chat.chatRoom;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.back.chat.chatMessage.ChatMessage;
import org.example.back.chat.chatMessage.ChatMessageRepository;

import org.example.back.chat.chatroommember.ChatRoomParticipant;
import org.example.back.chat.chatroommember.ChatRoomParticipantRepository;
import org.example.back.chat.common.dto.ChatDto;
import org.example.back.chat.common.dto.ChatMessageRes;
import org.example.back.chat.common.dto.ChatRoomRes;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomParticipantRepository chatRoomParticipantRepository;
	private final RabbitTemplate rabbitTemplate;
	private static final String ROUTING_KEY_PREFIX = "room.";

	@Override
	public ChatDto.ChatRoomCreateRes createChatRoomForPersonal(Long id, ChatDto.ChatRoomCreateReq request) {
		return null;
	}

	@Override
	public List<ChatRoomRes> getChatRooms(Long loginId) {
		return getChatRoomsByProfileId(loginId);
	}

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

	public List<ChatRoomRes> getChatRoomsByProfileId(Long profileId) {
		List<ChatRoomParticipant> participants = chatRoomParticipantRepository.findAllByProfileId(profileId);

		return participants.stream()
			.map(participant -> {
				ChatRoom chatRoom = participant.getChatRoom();
				Optional<ChatMessage> lastMessage = chatMessageRepository
					.findTopByChatRoomIdOrderByCreatedAtDesc(chatRoom.getId());
				int unreadCount = chatMessageRepository
					.countByChatRoomIdAndCreatedAtAfter(chatRoom.getId(), participant.getLastEntryTime());

				return ChatRoomRes.createRes(
					chatRoom.getId(),
					participant.getPartnerProfileId(),
					unreadCount,
					lastMessage
				);
			})
			.toList();
	}
}