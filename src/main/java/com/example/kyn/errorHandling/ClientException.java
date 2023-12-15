package com.example.kyn.errorHandling;

public class ClientException extends RuntimeException {

    private int statusCode;

    public ClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}