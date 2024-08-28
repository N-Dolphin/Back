package org.example.back.user.exception;

import org.example.back.exception.ClientErrorException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ClientErrorException {

    public UserNotFoundException(){
        super(HttpStatus.NOT_FOUND,"User not found");
    }

    public UserNotFoundException(String username){
        super(HttpStatus.NOT_FOUND,"User with username"+ username + "not found");
    }

}
