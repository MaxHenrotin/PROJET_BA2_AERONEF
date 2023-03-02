package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CallSignTest {

    @Test
    void callSignPatternWorks(){
        assertThrows(IllegalArgumentException.class, () -> new CallSign("AZGEFDBDGEDGDHEG"));

        assertThrows(IllegalArgumentException.class, () -> new CallSign("ASDJDG011"));

        assertThrows(IllegalArgumentException.class, () -> new CallSign("SHVDCD!0"));

        assertThrows(IllegalArgumentException.class, () -> new CallSign("abd43"));

        assertEquals(CallSign.class, (new CallSign("ABCD1234")).getClass());

    }

}