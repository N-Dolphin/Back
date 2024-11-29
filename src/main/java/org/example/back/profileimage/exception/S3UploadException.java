package org.example.back.profileimage.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class S3UploadException extends ClientErrorException {
	public S3UploadException(String message, Throwable cause) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, message);
	}
}
