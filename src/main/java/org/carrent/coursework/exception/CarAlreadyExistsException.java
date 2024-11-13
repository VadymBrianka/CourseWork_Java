package org.carrent.coursework.exception;

import jakarta.persistence.EntityExistsException;

public class CarAlreadyExistsException extends EntityExistsException {

    public CarAlreadyExistsException(String message) {
        super(message);
    }
}
