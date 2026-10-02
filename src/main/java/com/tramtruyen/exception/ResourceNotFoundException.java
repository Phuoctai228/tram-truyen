package com.tramtruyen.exception;

/** Indicates that a requested domain resource does not exist or is unavailable. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}