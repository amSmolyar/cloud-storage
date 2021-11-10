package ru.netology.diploma.pojo.exceptions;

public class FileDownloadException  extends Exception {
    private final int id;

    public FileDownloadException(String msg) {
        super(msg);
        id = 5;
    }

    public int getId() {
        return id;
    }
}
