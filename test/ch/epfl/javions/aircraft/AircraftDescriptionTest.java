package ch.epfl.javions.aircraft;

import ch.epfl.javions.adsb.CallSign;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDescriptionTest {
    @Test
    void aircraftDescriptionPatternWorks(){
        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription("AZGEFDBDGEDGDHEG"));

        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription("9"));

        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription("*ç%&/("));

        assertThrows(IllegalArgumentException.class, () -> new AircraftDescription(""));

        AircraftDescription d1= new AircraftDescription("A3J");
        assertEquals("A3J", d1.aircraftDescription());

    }

}