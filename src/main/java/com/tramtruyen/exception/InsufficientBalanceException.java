package com.tramtruyen.exception;

import lombok.Getter;

/**
 * Exception thrown when a user has insufficient coin balance to perform a transaction.
 */
@Getter
public class InsufficientBalanceException extends RuntimeException {

    private final Integer requiredCoins;
    private final Integer currentCoins;
    private final Integer shortfall;

    public InsufficientBalanceException(String message) {
        super(message);
        this.requiredCoins = null;
        this.currentCoins = null;
        this.shortfall = null;
    }

    public InsufficientBalanceException(int requiredCoins, int currentCoins) {
        super(String.format("Số dư Coin không đủ (Hiện có: %d Coin, Cần: %d Coin)", currentCoins, requiredCoins));
        this.requiredCoins = requiredCoins;
        this.currentCoins = currentCoins;
        this.shortfall = Math.max(0, requiredCoins - currentCoins);
    }

    public InsufficientBalanceException(String message, int requiredCoins, int currentCoins) {
        super(message);
        this.requiredCoins = requiredCoins;
        this.currentCoins = currentCoins;
        this.shortfall = Math.max(0, requiredCoins - currentCoins);
    }
}
