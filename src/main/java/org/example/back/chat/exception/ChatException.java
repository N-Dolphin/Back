package org.example.back.chat.exception;

public class ChatException extends RuntimeException {
	private final String errorCode;

	public ChatException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}