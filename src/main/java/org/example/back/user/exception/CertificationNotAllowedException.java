package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class CertificationNotAllowedException extends ClientErrorException {

	public CertificationNotAllowedException(String email) {
		super(HttpStatus.FORBIDDEN, email+ "에 해당하는 인증번호가 일치하지 않습니다");
	}

}
