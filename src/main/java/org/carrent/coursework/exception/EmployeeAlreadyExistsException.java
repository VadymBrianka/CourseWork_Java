package org.carrent.coursework.exception;

import jakarta.persistence.EntityExistsException;

public class EmployeeAlreadyExistsException extends EntityExistsException {

    public EmployeeAlreadyExistsException(String message) {
        super(message);
    }

}