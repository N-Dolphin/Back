package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ClientErrorException {

	public UserAlreadyExistsException() {
		super(HttpStatus.CONFLICT, "User already exists");
	}

	public UserAlreadyExistsException(String email) {
		super(HttpStatus.CONFLICT, "User with email" + email + "already exists");
	}

}
