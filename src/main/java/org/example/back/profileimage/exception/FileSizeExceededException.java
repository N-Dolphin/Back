package org.example.back.profileimage.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class FileSizeExceededException extends ClientErrorException {
	public FileSizeExceededException(String message) {
		super(HttpStatus.BAD_REQUEST, message);
	}
}