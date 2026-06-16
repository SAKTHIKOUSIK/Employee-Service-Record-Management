package com.employee.backend.exception;

public class DuplicateEmployeeCodeException extends RuntimeException {

    public DuplicateEmployeeCodeException(String message) {
        super(message);
    }
}