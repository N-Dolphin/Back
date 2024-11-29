package org.example.back.profileimage.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class FileUploadException extends ClientErrorException {
	public FileUploadException(String message, Throwable cause) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}
}
