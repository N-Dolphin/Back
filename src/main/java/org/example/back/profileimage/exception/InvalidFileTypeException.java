package org.example.back.profileimage.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends ClientErrorException {
	public InvalidFileTypeException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}