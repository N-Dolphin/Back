package org.example.back.chat.config;

import org.example.back.chat.exception.ChatException;
import org.example.back.rabbitmq.MessageDto;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class MessageValidator {
	private static final int MAX_MESSAGE_LENGTH = 1000;

	public void validateMessage(MessageDto messageDto) {
		if (messageDto == null) {
			throw new ChatException("INVALID_MESSAGE", "메시지가 비어있습니다.");
		}
		if (StringUtils.isBlank(messageDto.content())) {
			throw new ChatException("INVALID_CONTENT", "메시지 내용이 비어있습니다.");
		}
		if (messageDto.content().length() > MAX_MESSAGE_LENGTH) {
			throw new ChatException("CONTENT_TOO_LONG",
				"메시지 길이가 제한을 초과했습니다. (최대 " + MAX_MESSAGE_LENGTH + "자)");
		}
	}
}