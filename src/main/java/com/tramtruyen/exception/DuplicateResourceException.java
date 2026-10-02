package com.tramtruyen.exception;

/** Indicates that a resource conflicts with an existing unique business value. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}