package org.example.back.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageDto(
	String content,
	LocalDateTime sentAt
) {
}