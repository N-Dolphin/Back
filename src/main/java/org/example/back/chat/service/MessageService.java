package org.example.back.chat.service;

import java.util.List;

import org.example.back.chat.config.ChatMessageMapper;
import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.chat.exception.ChatException;
import org.example.back.chat.repository.ChatMessageRepository;
import org.example.back.rabbitmq.MessageDto;
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
	public Long sendMessage(Long fromProfileId, Long toProfileId, String content, Long chatRoomId) {
		log.info("Processing message - from: {}, to: {}, room: {}",
			fromProfileId, toProfileId, chatRoomId);

		ChatRoom chatRoom = chatRoomService.getChatRoom(chatRoomId);
		ChatMessage chatMessage = ChatMessage.of(fromProfileId, toProfileId, content, chatRoom);
		chatMessage = messageRepository.save(chatMessage);

		try {
			MessageDto messageDto = chatMessageMapper.toDto(chatMessage);
			messageBrokerService.publishMessage(chatRoomId, messageDto);
			chatRoomService.updateRoomActivity(chatRoomId);
			return chatMessage.getId();
		} catch (Exception e) {
			log.error("Failed to send message", e);
			chatMessage.setStatus(ChatMessage.MessageStatus.FAILED);
			messageRepository.save(chatMessage);
			throw new ChatException("MESSAGE_SEND_FAILED", "메시지 전송에 실패했습니다: " + e.getMessage());
		}
	}

	public List<MessageDto> getMessages(Long chatRoomId, int page, int size) {
		List<ChatMessage> messages = messageRepository.findByChatRoomId(chatRoomId)
			.stream()
			.skip(page * size)
			.limit(size)
			.toList();
		return messages.stream()
			.map(chatMessageMapper::toDto)
			.toList();
	}

	public boolean hasMoreMessages(Long chatRoomId, int page, int size) {
		long count = messageRepository.countByChatRoomId(chatRoomId);
		return (page + 1) * size < count;
	}

}