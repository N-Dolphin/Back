package org.example.back.profile.exception;

public class InternalServerErrorException extends RuntimeException {
	public InternalServerErrorException(String message) {
		super(message);
	}
}