package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ClientErrorException {
	public InvalidPasswordException() {
		super(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다");
	}
}