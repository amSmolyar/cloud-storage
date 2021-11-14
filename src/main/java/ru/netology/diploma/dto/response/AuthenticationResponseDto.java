package ru.netology.diploma.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class AuthenticationResponseDto {
    @JsonProperty("auth-token")
    private String token;

    @JsonCreator
    public AuthenticationResponseDto(String token) {
        this.token = token;
    }
}
