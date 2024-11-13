package org.carrent.coursework.exception;

import jakarta.persistence.EntityNotFoundException;

public class OrderNotFoundException extends EntityNotFoundException {

    public OrderNotFoundException(String message){
        super(message);
    }
}
