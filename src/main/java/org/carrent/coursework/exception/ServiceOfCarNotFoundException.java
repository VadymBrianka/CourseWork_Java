package org.carrent.coursework.exception;

import jakarta.persistence.EntityNotFoundException;

public class ServiceOfCarNotFoundException extends EntityNotFoundException {

    public ServiceOfCarNotFoundException(String message){
        super(message);
    }
}
