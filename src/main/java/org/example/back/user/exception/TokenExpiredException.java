package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends ClientErrorException {
	public TokenExpiredException() {
		super(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");
	}
}