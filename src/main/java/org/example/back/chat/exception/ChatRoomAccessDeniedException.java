package org.example.back.chat.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ChatRoomAccessDeniedException extends ClientErrorException {
	public ChatRoomAccessDeniedException(String message) {
		super(HttpStatus.FORBIDDEN, message);  // 403 FORBIDDEN 상태 코드 사용
	}
}