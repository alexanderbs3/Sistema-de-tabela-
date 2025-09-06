package com.leaderboard.exception;

import java.util.Map;

public class ValidationException extends RuntimeException{
    private Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}

