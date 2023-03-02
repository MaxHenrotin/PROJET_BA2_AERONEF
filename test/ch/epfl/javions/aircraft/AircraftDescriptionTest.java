package ch.epfl.javions.aircraft;

import ch.epfl.javions.adsb.CallSign;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDescriptionTest {
    @Test
    void aircraftDescriptionPatternWorks(){
        assertThrows(IllegalArgumentException.class, () -> new CallSign("AZGEFDBDGEDGDHEG"));

        assertThrows(IllegalArgumentException.class, () -> new CallSign("9"));

        assertThrows(IllegalArgumentException.class, () -> new CallSign("*ç%&/("));

        assertEquals(AircraftDescription.class, (new AircraftDescription("A-9")).getClass());

    }

}