package com.securebank.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PinHasher {
    private static final int COST = 12;

    private PinHasher() {}

    public static boolean isValidFormat(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    public static String hash(String pin) {
        if (pin == null) {
            throw new NullPointerException("PIN must not be null");
        }
        return BCrypt.withDefaults().hashToString(COST, pin.toCharArray());
    }

    public static boolean verify(String pin, String stored) {
        if (pin == null || stored == null) {
            return false;
        }

        try {
            return BCrypt.verifyer().verify(pin.toCharArray(), stored).verified;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }
}
