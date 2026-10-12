package com.tramtruyen.exception;

/**
 * Exception thrown when a user has insufficient coin balance to perform a transaction.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
