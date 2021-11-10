package ru.netology.diploma.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.diploma.dto.response.ExceptionResponseDto;
import ru.netology.diploma.pojo.exceptions.FileDeleteException;
import ru.netology.diploma.pojo.exceptions.FileRewriteException;
import ru.netology.diploma.pojo.exceptions.FileUploadException;
import ru.netology.diploma.pojo.exceptions.InputDataException;
import ru.netology.diploma.security.jwt.JwtAuthenticationException;

import javax.servlet.ServletException;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    public static final int RUNTIME_EXCEPTION_ID = 20;
    public static final int AUTHENTICATION_EXCEPTION_ID = 10;
    public static final int SERVLET_EXCEPTION_ID = 30;
    public static final int IO_EXCEPTION_ID = 40;
    public static final int FILE_NOT_FOUND_EXCEPTION_ID = 50;

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handlerJwtAuthentication(JwtAuthenticationException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), e.getId());
        return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponseDto> handlerAuthentication(AuthenticationException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), AUTHENTICATION_EXCEPTION_ID);
        return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InputDataException.class)
    public ResponseEntity<ExceptionResponseDto> handlerInputDataException(InputDataException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), e.getId());
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponseDto> handlerRuntimeException(RuntimeException e) {
        e.printStackTrace();
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), RUNTIME_EXCEPTION_ID);
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ExceptionResponseDto> handlerServletException(ServletException e) {
        e.printStackTrace();
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), SERVLET_EXCEPTION_ID);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ExceptionResponseDto> handlerFileUploadException(FileUploadException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), e.getId());
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileRewriteException.class)
    public ResponseEntity<ExceptionResponseDto> handlerFileRewriteException(FileRewriteException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), e.getId());
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileDeleteException.class)
    public ResponseEntity<ExceptionResponseDto> handlerFileDeleteException(FileDeleteException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), e.getId());
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handlerFileNotFoundException(FileNotFoundException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), FILE_NOT_FOUND_EXCEPTION_ID);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ExceptionResponseDto> handlerIOException(IOException e) {
        ExceptionResponseDto resp = new ExceptionResponseDto(e.getMessage(), IO_EXCEPTION_ID);
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }


}
