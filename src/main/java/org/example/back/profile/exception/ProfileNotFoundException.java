package org.example.back.profile.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class ProfileNotFoundException extends ClientErrorException {

	public ProfileNotFoundException() {
		super(HttpStatus.NOT_FOUND, "Profile not found");
	}



}
