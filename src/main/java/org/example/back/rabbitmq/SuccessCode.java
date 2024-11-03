package org.example.back.rabbitmq;

// SuccessCode.java

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
	SELECT(200, "Request successful"),
	CREATED(201, "Resource created successfully");

	private final int status;
	private final String message;

}
