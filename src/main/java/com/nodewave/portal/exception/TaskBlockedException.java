package com.nodewave.portal.exception;

import org.springframework.http.HttpStatus;

public class TaskBlockedException extends ApiException {

    public TaskBlockedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
