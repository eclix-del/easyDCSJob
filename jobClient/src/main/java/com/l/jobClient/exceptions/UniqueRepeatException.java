package com.l.jobClient.exceptions;

public class UniqueRepeatException extends RuntimeException {

    private String message;

    public UniqueRepeatException(String message) {
        super(message);
        this.message = message;
    }

    public UniqueRepeatException() {
        super();
    }

}
