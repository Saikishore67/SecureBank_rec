package com.securebank.exception;

public class InvalidPinException extends Exception{
    private final int attemptsRemaining;

//    Entered Wrong PIN
    public InvalidPinException(int attemptsRemaining) {
        super("Invalid PIN. " + attemptsRemaining + " attempt(s) left.");
        this.attemptsRemaining = attemptsRemaining;
    }

//    Entered something other than PIN
    public InvalidPinException(String message) {
        super(message);
        this.attemptsRemaining = -1;
    }

//    Returning number of attempts
    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }
}
