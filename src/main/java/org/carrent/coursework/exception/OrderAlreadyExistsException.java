package org.carrent.coursework.exception;

import jakarta.persistence.EntityExistsException;

public class OrderAlreadyExistsException extends EntityExistsException {

  public OrderAlreadyExistsException(String message) {
        super(message);
    }

}