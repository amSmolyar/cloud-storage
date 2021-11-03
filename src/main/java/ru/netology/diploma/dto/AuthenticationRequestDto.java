package ru.netology.diploma.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class AuthenticationRequestDto {
    private String login;
    private String password;

    @JsonCreator
    public AuthenticationRequestDto(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
