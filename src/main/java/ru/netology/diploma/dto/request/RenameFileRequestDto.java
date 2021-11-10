package ru.netology.diploma.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class RenameFileRequestDto {

    @Pattern(regexp = "[a-zA-Z0-9._ -]+\\.\\w+$")
    private String filename;

    @JsonCreator
    public RenameFileRequestDto(String filename) {
        this.filename = filename;
    }
}
