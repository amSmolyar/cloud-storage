package ru.netology.diploma.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.diploma.dto.ExceptionResponseDto;
import ru.netology.diploma.security.jwt.JwtAuthenticationException;

@RestControllerAdvice
public class ExceptionControllerAdvice {
    public static final int RUNTIME_EXCEPTION_ID = 20;
    public static final int AUTHENTICATION_EXCEPTION_ID = 10;

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handlerJwtAuthentication(JwtAuthenticationException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), e.getId());
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handlerAuthentication(AuthenticationException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), AUTHENTICATION_EXCEPTION_ID);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponseDto> handlerJRuntimeException(RuntimeException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), RUNTIME_EXCEPTION_ID);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }


}
