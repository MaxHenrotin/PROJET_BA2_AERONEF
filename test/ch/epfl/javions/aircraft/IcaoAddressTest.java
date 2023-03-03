package ch.epfl.javions.aircraft;

import ch.epfl.javions.adsb.CallSign;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IcaoAddressTest {
    @Test
    void assertThrowsIfIcaoAddressIsWrongSize() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("26F0A13"));
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("284B1"));
    }

    @Test
    void assertThrowsIfIcaoAddressHasWrongCharacter() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress("VAnsH5"));
    }

    @Test
    void assertThrowsIfIcaoAddressIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoAddress(""));
    }
}