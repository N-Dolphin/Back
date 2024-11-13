package org.example.back.rabbitmq;

import java.time.LocalDateTime;

import org.example.back.chat.entity.ChatMessage;

public record MessageDto(
	Long messageId,      // 메시지 ID 추가
	Long fromProfileId,
	Long toProfileId,
	String content,
	LocalDateTime sendAt,
	Long chatRoomId,
	ChatMessage.MessageStatus status, // 메시지 상태 추가
	LocalDateTime deliveredAt,
	LocalDateTime readAt
) {
	// 메시지 ID 추가를 위한 메서드
	public MessageDto withMessageId(Long messageId) {
		return new MessageDto(
			messageId,
			this.fromProfileId,
			this.toProfileId,
			this.content,
			this.sendAt,
			this.chatRoomId,
			this.status,
			this.deliveredAt,
			this.readAt
		);
	}
}