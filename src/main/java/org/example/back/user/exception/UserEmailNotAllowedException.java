package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class UserEmailNotAllowedException extends ClientErrorException {

	public UserEmailNotAllowedException(String email) {
		super(HttpStatus.FORBIDDEN, "이메일이 일치하지 않습니다");
	}

}
