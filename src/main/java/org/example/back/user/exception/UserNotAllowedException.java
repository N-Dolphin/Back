package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class UserNotAllowedException extends ClientErrorException {

    public UserNotAllowedException(){
        super(HttpStatus.FORBIDDEN,"User not allowed");
    }



}
