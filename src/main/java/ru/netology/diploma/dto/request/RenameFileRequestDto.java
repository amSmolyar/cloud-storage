package ru.netology.diploma.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RenameFileRequestDto {

    @NotNull
    private String filename;

    @JsonCreator
    public RenameFileRequestDto(String filename) {
        this.filename = filename;
    }
}
