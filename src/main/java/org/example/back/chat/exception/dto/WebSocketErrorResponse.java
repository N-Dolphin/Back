package org.example.back.chat.exception.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class WebSocketErrorResponse {
	private final String code;
	private final String message;
	private final LocalDateTime timestamp;

	public WebSocketErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
		this.timestamp = LocalDateTime.now();
	}
}