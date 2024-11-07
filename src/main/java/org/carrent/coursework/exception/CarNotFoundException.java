package org.carrent.coursework.exception;


import jakarta.persistence.EntityNotFoundException;


public class CarNotFoundException extends EntityNotFoundException {

    public CarNotFoundException(String message){
        super(message);
    }


}
