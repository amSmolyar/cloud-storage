package ru.netology.diploma.pojo.exceptions;

public class FileRewriteException extends Exception {
    private final int id;
    public FileRewriteException(String msg) {
        super(msg);
        id = 4;
    }

    public int getId() {
        return id;
    }
}
