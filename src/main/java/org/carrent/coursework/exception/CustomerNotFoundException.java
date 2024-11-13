package org.carrent.coursework.exception;

import jakarta.persistence.EntityNotFoundException;

public class CustomerNotFoundException extends EntityNotFoundException {

    public CustomerNotFoundException(String message){
        super(message);
    }

}
