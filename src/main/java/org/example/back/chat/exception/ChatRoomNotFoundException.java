package org.example.back.chat.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;


public class ChatRoomNotFoundException extends ClientErrorException {
	public ChatRoomNotFoundException(String message) {
		super(HttpStatus.FORBIDDEN, message);  // 403 FORBIDDEN 상태 코드 사용
	}
}