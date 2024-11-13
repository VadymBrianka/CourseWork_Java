package org.carrent.coursework.exception;

import jakarta.persistence.EntityExistsException;

public class CustomerAlreadyExistsException extends EntityExistsException {

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }

}
