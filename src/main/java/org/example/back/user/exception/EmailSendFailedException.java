package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class EmailSendFailedException extends ClientErrorException {
	public EmailSendFailedException() {
		super(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다");
	}
}