package com.nodewave.portal.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApiException {

    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
