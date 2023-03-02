package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDatabaseTest {

    @Test
    void assertThrowsIfWrongZip() {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/abfkh76S.zip");
        assertThrows(IOException.class, () -> aircraftDatabase.get(new IcaoAddress("A040C6")));
        AircraftDatabase aircraftDatabase2 = new AircraftDatabase("aircraft.zip");
        assertThrows(IOException.class, () -> aircraftDatabase2.get(new IcaoAddress("A040C6")));
        AircraftDatabase aircraftDatabase3 = new AircraftDatabase("aircraft");
        assertThrows(IOException.class, () -> aircraftDatabase3.get(new IcaoAddress("A040C6")));
        AircraftDatabase aircraftDatabase4 = new AircraftDatabase("jfakge");
        assertThrows(IOException.class, () -> aircraftDatabase4.get(new IcaoAddress("A040C6")));
    }

    @Test
    void assertThrowsIfZipAddressIsEmpty() {
        assertThrows(NullPointerException.class, () -> new AircraftDatabase(""));
    }

    @Test
    void assertThrowsIfZipAddressIsNull() {
        assertThrows(NullPointerException.class, () -> new AircraftDatabase(null));
    }

    @Test
    void aircraftDatabaseTest1() {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        try {
            AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A040C6"));
            System.out.println(aircraftData);   //possible car AircraftData est un enregistrement
            assertEquals(new AircraftData(new AircraftRegistration("N115WM"), new AircraftTypeDesignator("GLST"), "GLASAIR SH-4 GlaStar", new AircraftDescription("L1P"), WakeTurbulenceCategory.LIGHT), aircraftData);
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
            int repetitionNumber = 1000;  //on teste 1000 cas aléatoires d'adresses ICAO
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

    @Test
    void aircraftDatabaseTestCasSpecial() { //trouvé grâce à aircraftDatabaseTestGeneral()
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        try {
            AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A8E250"));
            System.out.println(aircraftData);
            aircraftData = aircraftDatabase.get(new IcaoAddress("C07848"));
            System.out.println(aircraftData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}