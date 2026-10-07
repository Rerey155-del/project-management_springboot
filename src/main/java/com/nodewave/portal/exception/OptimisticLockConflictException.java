package com.nodewave.portal.exception;

import org.springframework.http.HttpStatus;

public class OptimisticLockConflictException extends ApiException {

    public OptimisticLockConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
