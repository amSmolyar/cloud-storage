package ru.netology.diploma.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AuthenticationRequestDto {
    @NotNull
    @Size(min = 4)
    private String login;

    @NotNull
    @Size(min = 5)
    private String password;

    @JsonCreator
    public AuthenticationRequestDto(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
