package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDataTest {

    @Test
    void aircraftDataThrowsIfTypeDesignatorIsNull() {
        assertThrows(NullPointerException.class, () -> new AircraftData(new AircraftRegistration("N115WM"), null, "GLASAIR SH-4 GlaStar", new AircraftDescription("L1P"), WakeTurbulenceCategory.LIGHT));
    }

}