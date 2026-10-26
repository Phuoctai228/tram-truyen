package com.tramtruyen.exception;

/**
 * Thrown when an unlock is attempted on a chapter that cannot be purchased
 * (e.g. draft, locked, deleted, or novel is archived).
 */
public class ChapterNotPurchasableException extends RuntimeException {

    public ChapterNotPurchasableException(String message) {
        super(message);
    }
}
