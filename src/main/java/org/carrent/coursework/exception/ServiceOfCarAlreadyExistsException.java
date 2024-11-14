package org.carrent.coursework.exception;

import jakarta.persistence.EntityExistsException;

public class ServiceOfCarAlreadyExistsException extends EntityExistsException {

    public ServiceOfCarAlreadyExistsException(String message) {
        super(message);
    }

}
