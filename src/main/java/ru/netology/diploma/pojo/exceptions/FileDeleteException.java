package ru.netology.diploma.pojo.exceptions;

public class FileDeleteException extends Exception {
    private final int id;

    public FileDeleteException(String msg) {
        super(msg);
        id = 6;
    }

    public int getId() {
        return id;
    }
}
