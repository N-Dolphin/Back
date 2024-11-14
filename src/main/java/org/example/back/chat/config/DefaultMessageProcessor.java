package org.example.back.chat.config;

import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.repository.ChatMessageRepository;
import org.example.back.chat.repository.ChatRoomRepository;
import org.example.back.chat.service.ChatRoomService;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultMessageProcessor implements MessageProcessor {
	private final ChatMessageRepository messageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomService chatRoomService;

	@Override
	public ProcessingResult processMessage(MessageDto messageDto) {
		// 채팅방 정보 조회
		Long chatRoomId = messageDto.chatRoomId();
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new IllegalArgumentException("Chat room not found!"));
		log.info("Chat room found: {}", chatRoom.getId());

		// 송수신자 ID 결정
		Long fromProfileId;
		Long toProfileId;
		if (chatRoom.getFromProfileId().equals(messageDto.fromProfileId())) {
			fromProfileId = chatRoom.getFromProfileId();
			toProfileId = chatRoom.getToProfileId();
		} else {
			fromProfileId = chatRoom.getToProfileId();
			toProfileId = chatRoom.getFromProfileId();
		}

		ChatMessage chatMessage = ChatMessage.of(fromProfileId, toProfileId,
			messageDto.content(), chatRoom);
		chatMessage = messageRepository.save(chatMessage);
		chatRoomService.updateRoomActivity(chatRoom.getId());

		return new ProcessingResult(chatMessage, chatRoom);
	}

	@Override
	public void handleError(ChatMessage chatMessage, Exception e) {
		log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
		chatMessage.setStatus(ChatMessage.MessageStatus.FAILED);
		messageRepository.save(chatMessage);
	}
}