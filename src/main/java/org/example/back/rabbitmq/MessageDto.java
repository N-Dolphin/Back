package org.example.back.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDto(
	Long fromProfileId,
	Long toProfileId,

	@JsonProperty("content")
	String content
) {
}
