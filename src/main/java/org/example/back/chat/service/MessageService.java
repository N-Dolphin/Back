package org.example.back.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.back.chat.config.ChatMessageMapper;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.exception.ChatException;
import org.example.back.chat.repository.ChatMessageRepository;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {
	private final ChatMessageRepository messageRepository;
	private final MessageBrokerService messageBrokerService;
	private final ChatRoomService chatRoomService;
	private final ChatMessageMapper chatMessageMapper;


	@Transactional
	public void sendMessage(Long fromProfileId, Long toProfileId, String content, Long chatRoomId) {
		log.info("Processing message - from: {}, to: {}, room: {}",
			fromProfileId, toProfileId, chatRoomId);

		// DB 저장 없이 메시지 발행만 수행
		ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId);
		MessageDto messageDto = new MessageDto(
			null,                    // messageId는 Consumer에서 저장 후 설정
			fromProfileId,
			toProfileId,
			content,
			LocalDateTime.now(),
			chatRoomId,
			ChatMessage.MessageStatus.SENT,
			null,
			null
		);

		try {
			messageBrokerService.publishMessage(chatRoomId, messageDto);
			chatRoomService.updateRoomActivity(chatRoomId);
		} catch (Exception e) {
			log.error("Failed to send message", e);
			throw new ChatException("MESSAGE_SEND_FAILED",
				"메시지 전송에 실패했습니다: " + e.getMessage());
		}
	}

	public List<MessageDto> getMessages(Long chatRoomId, int page, int size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		List<ChatMessage> messages = messageRepository.findByChatRoomId(chatRoomId, pageRequest);
		return messages.stream()
			.map(chatMessageMapper::toDto)
			.toList();
	}

	public List<MessageDto> getMessagesBefore(Long chatRoomId, Long cursor, int size) {
		PageRequest pageRequest = PageRequest.of(0, size);
		List<ChatMessage> messages = messageRepository.findByChatRoomIdAndIdLessThan(
			chatRoomId, cursor, pageRequest);
		return messages.stream()
			.map(chatMessageMapper::toDto)
			.toList();
	}

	public boolean hasMoreMessages(Long chatRoomId, int page, int size) {
		long count = messageRepository.countByChatRoomId(chatRoomId);
		return (page + 1) * size < count;
	}
}