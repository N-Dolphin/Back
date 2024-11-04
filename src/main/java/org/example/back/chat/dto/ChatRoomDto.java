package org.example.back.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomDto(
	Long id,
	Long fromProfileId,
	Long toProfileId,
	LocalDateTime createdAt
) {
}