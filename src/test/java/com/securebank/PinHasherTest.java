package com.securebank;

import com.securebank.util.PinHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PinHasherTest {

    @Test
    void correctPinVerifies() {
        String stored = PinHasher.hash("1234");
        assertTrue(PinHasher.verify("1234", stored));
    }

    @Test
    void wrongPinFails() {
        String stored = PinHasher.hash("1234");
        assertFalse(PinHasher.verify("4321", stored));
    }

    @Test
    void samePinGivesDifferentHashes() {
        assertNotEquals(PinHasher.hash("1234"), PinHasher.hash("1234"));
    }

    @Test
    void storedValueIsNotPlainPin() {
        assertFalse(PinHasher.hash("1234").contains("1234"));
    }

    @Test
    void malformedStoredValueFailsSafely() {
        assertFalse(PinHasher.verify("1234", "garbage"));
        assertFalse(PinHasher.verify("1234", null));
        assertFalse(PinHasher.verify(null, PinHasher.hash("1234")));
    }

    @Test
    void formatCheck() {
        assertTrue(PinHasher.isValidFormat("0420"));
        assertFalse(PinHasher.isValidFormat("123"));
        assertFalse(PinHasher.isValidFormat("12a4"));
        assertFalse(PinHasher.isValidFormat(null));
    }

    @Test
    void hashIsBcryptFormat() {
        String stored = PinHasher.hash("1234");
        assertTrue(stored.startsWith("$2"));
        assertEquals(60, stored.length());
    }
}