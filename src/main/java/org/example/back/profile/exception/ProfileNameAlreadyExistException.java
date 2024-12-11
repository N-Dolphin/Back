package org.example.back.profile.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ProfileNameAlreadyExistException extends ClientErrorException {
	public ProfileNameAlreadyExistException() {
		super(HttpStatus.CONFLICT, "Profile name already exists");
	}
}
