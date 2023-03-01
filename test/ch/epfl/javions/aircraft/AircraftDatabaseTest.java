package ch.epfl.javions.aircraft;
//  Author:    Max Henrotin

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AircraftDatabaseTest {

    @Test
    void aircraftDatabaseTest() {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("aircraft.zip");
        try {
            AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A00001"));
            System.out.println(aircraftData);   //possible car AircraftData est un enregistrement
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
