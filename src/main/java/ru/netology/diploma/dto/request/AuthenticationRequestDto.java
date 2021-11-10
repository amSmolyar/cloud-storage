package ru.netology.diploma.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AuthenticationRequestDto {
    @NotNull
    private String login;
    @NotNull
    private String password;

    @JsonCreator
    public AuthenticationRequestDto(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
