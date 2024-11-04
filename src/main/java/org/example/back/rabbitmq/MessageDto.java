package org.example.back.rabbitmq;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDto(
	Long fromProfileId,
	Long toProfileId,

	@JsonProperty("content")
	String content,

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime sendAt
) {
}
