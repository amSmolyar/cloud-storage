package ru.netology.diploma.pojo.exceptions;

public class InputDataException extends RuntimeException {
    private final int id;
    public InputDataException(String msg) {
        super(msg);
        id = 2;
    }

    public int getId() {
        return id;
    }
}
