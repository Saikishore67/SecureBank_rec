package com.securebank.exception;

public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 1L;

    private final double requestedAmount;
    private final double availableAmount;

    public InsufficientFundsException(String message, double requestedAmount, double availableAmount) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.availableAmount = availableAmount;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getAvailableAmount() {
        return availableAmount;
    }

    public double getShortfall() {
        return requestedAmount - availableAmount;
    }
}