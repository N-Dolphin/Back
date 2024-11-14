package org.example.back.chat.config;

import org.example.back.chat.entity.ChatMessage;
import org.example.back.chat.entity.ChatRoom;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatMessageMapper {
	public MessageDto toDto(ChatMessage message) {
		return new MessageDto(
			message.getId(),                // messageId
			message.getSenderId(),          // fromProfileId
			message.getReceiverId(),        // toProfileId
			message.getContent(),           // content
			message.getSentAt(),            // sendAt
			message.getChatRoom().getId(),  // chatRoomId
			message.getStatus(),            // status
			message.getDeliveredAt(),       // deliveredAt
			message.getReadAt()             // readAt
		);
	}

	public ChatMessage toEntity(MessageDto dto, ChatRoom chatRoom) {
		ChatMessage message = ChatMessage.of(
			dto.fromProfileId(),
			dto.toProfileId(),
			dto.content(),
			chatRoom
		);
		message.setStatus(dto.status());
		message.setDeliveredAt(dto.deliveredAt());
		message.setReadAt(dto.readAt());
		return message;
	}
}