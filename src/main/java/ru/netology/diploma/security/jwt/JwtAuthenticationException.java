package ru.netology.diploma.security.jwt;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
    private final int id;
    public JwtAuthenticationException(String msg) {
        super(msg);
        id = 11;
    }

    public int getId() {
        return id;
    }
}
