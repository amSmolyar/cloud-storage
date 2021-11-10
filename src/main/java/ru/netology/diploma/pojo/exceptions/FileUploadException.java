package ru.netology.diploma.pojo.exceptions;


public class FileUploadException extends Exception {
    private final int id;
    public FileUploadException(String msg) {
        super(msg);
        id = 3;
    }

    public int getId() {
        return id;
    }
}
