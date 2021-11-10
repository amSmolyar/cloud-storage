package ru.netology.diploma.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExceptionResponseDto {
    private final String message;
    private final int id;

    @JsonCreator
    public ExceptionResponseDto(String message, int id) {
        this.message = message;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
