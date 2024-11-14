package org.example.back.chat.dto;

import java.time.LocalDateTime;

import org.example.back.chat.entity.ChatRoom;

public record ChatRoomInfo(
	Long id,
	Long fromProfileId,
	Long toProfileId,
	LocalDateTime lastActivity,
	boolean isActive,
	LocalDateTime createdAt
) {
	public static ChatRoomInfo from(ChatRoom chatRoom) {
		return new ChatRoomInfo(
			chatRoom.getId(),
			chatRoom.getFromProfileId(),
			chatRoom.getToProfileId(),
			chatRoom.getLastActivity(),
			chatRoom.isActive(),
			chatRoom.getCreatedAt()
		);
	}
}
