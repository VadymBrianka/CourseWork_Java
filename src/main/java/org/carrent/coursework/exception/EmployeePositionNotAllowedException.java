package org.carrent.coursework.exception;

public class EmployeePositionNotAllowedException extends RuntimeException {
    public EmployeePositionNotAllowedException(String message) {
        super(message);
    }
}
