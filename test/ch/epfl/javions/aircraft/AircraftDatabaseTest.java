package ch.epfl.javions.aircraft;
//  Author:    Max Henrotin

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AircraftDatabaseTest {

    @Test
    void aircraftDatabaseTest1() {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        try {
            AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A040C6"));
            System.out.println(aircraftData);   //possible car AircraftData est un enregistrement
            //assertEquals(new AircraftData(new AircraftRegistration("N115WM"), new AircraftTypeDesignator("GLST"), "GLASAIR SH-4 GlaStar", new AircraftDescription("L1P"), WakeTurbulenceCategory.of("L")), aircraftData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void aircraftDatabaseTestLimite() {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        try {
            AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("FFFFFF"));
            System.out.println(aircraftData);
            aircraftData = aircraftDatabase.get(new IcaoAddress("A00000"));
            System.out.println(aircraftData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void aircraftDatabaseTestGeneral() {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        try {
            int repetitionNumber = 100000;  //on teste 100000 cas aléatoires d'adresses ICAO
            do {
                String icaoAddress = "";
                for (int i = 0; i < 6; i++) {    //pour les 6 chiffres de l'adresse Icao
                    int randomNumber = (int) (16 * Math.random());    //random number between 0 and 15 (tous les hexadécimaux)
                    icaoAddress += Integer.toHexString(randomNumber).toUpperCase();
                }
                AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress(icaoAddress));
                System.out.println(aircraftData);   //possible car AircraftData est un enregistrement
                --repetitionNumber;
            }while(repetitionNumber > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
