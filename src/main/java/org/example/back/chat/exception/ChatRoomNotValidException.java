package org.example.back.chat.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ChatRoomNotValidException extends ClientErrorException {
	public ChatRoomNotValidException(String message) {
		super(HttpStatus.BAD_REQUEST, message);  // 403 FORBIDDEN 상태 코드 사용
	}
}