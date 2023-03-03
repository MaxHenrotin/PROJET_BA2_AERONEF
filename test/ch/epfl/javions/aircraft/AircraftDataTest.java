package ch.epfl.javions.aircraft;

import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftRegistration;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDataTest {

    @Test
    void aircraftDataThrowsIfTypeDesignatorIsNull() {
        assertThrows(NullPointerException.class, () -> new AircraftData(new AircraftRegistration("N115WM"), null, "GLASAIR SH-4 GlaStar", new AircraftDescription("L1P"), WakeTurbulenceCategory.LIGHT));
    }

}