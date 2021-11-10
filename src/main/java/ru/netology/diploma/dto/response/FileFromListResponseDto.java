package ru.netology.diploma.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class FileFromListResponseDto {
    private String filename;
    private int size;

    @JsonCreator
    public FileFromListResponseDto(String filename, int size) {
        this.filename = filename;
        this.size = size;
    }
}
