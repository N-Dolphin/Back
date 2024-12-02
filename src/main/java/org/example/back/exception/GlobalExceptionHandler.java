package org.example.back.exception;

import java.util.stream.Collectors;

import org.example.back.profile.exception.BadRequestException;
import org.example.back.profile.exception.ConflictException;
import org.example.back.profile.exception.InternalServerErrorException;
import org.example.back.profile.exception.UnauthorizedException;
import org.example.back.user.exception.InvalidTokenException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<ClientErrorResponse> handleChatException(RedisConnectionFailureException ex) {
        return new ResponseEntity<>(
            new ClientErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ClientErrorResponse> handleClientErrorException(ClientErrorException e){
        return new ResponseEntity<>(new ClientErrorResponse(e.getStatus(),e.getMessage()),e.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ClientErrorResponse> handleClientErrorException(MethodArgumentNotValidException e){
        var errorMessage=e.getFieldErrors().stream().map(fieldError -> fieldError.getField() + ": "+ fieldError.getDefaultMessage()).toList().toString();
        return new ResponseEntity<>(
                new ClientErrorResponse(HttpStatus.BAD_REQUEST, errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ClientErrorResponse> handleClientErrorException(HttpMessageNotReadableException e){
        return new ResponseEntity<>(
                new ClientErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ClientErrorResponse> handleRuntimeException(RuntimeException e){
        return ResponseEntity.internalServerError().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ClientErrorResponse> handleException(Exception e) {
        return ResponseEntity.internalServerError().build();

    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ClientErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return new ResponseEntity<>(
            new ClientErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage()),
            HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ClientErrorResponse> handleBadRequest(BadRequestException e) {
        return new ResponseEntity<>(
            new ClientErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ClientErrorResponse> handleConflict(ConflictException e) {
        return new ResponseEntity<>(
            new ClientErrorResponse(HttpStatus.CONFLICT, e.getMessage()),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ClientErrorResponse> handleInternalServerError(InternalServerErrorException e) {
        return new ResponseEntity<>(
            new ClientErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

