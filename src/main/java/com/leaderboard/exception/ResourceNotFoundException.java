package com.leaderboard.exception;

public class ResourceNotFoundException extends RuntimeException{
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s não encontrado(a) com %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    // Getters
    public String getResourceName() { return resourceName; }
    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}