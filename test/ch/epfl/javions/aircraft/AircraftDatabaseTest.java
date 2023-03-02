package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDatabaseTest {

    @Test
    void assertThrowsIfZipAddressIsEmpty() {
        assertThrows(NullPointerException.class, () -> new AircraftDatabase(""));
    }

    @Test
    void assertThrowsIfZipAddressIsNull() {
        assertThrows(NullPointerException.class, () -> new AircraftDatabase(null));
    }

    @Test
    void aircraftDatabaseTestAdresseIcaoOk() throws IOException {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A040C6"));
        System.out.println(aircraftData);   //possible car AircraftData est un enregistrement
        assertEquals(new AircraftData(new AircraftRegistration("N115WM"), new AircraftTypeDesignator("GLST"), "GLASAIR SH-4 GlaStar", new AircraftDescription("L1P"), WakeTurbulenceCategory.LIGHT), aircraftData);
    }

    @Test
    void aircraftDatabaseTestLimite() throws IOException {
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("FFFFFF"));
        System.out.println(aircraftData);
        aircraftData = aircraftDatabase.get(new IcaoAddress("A00000"));
        System.out.println(aircraftData);
    }

    @Test
    void aircraftDatabaseTestGeneral() throws IOException{
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
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
    }

    @Test
    void aircraftDatabaseTestCasSpecial() throws IOException{ //trouvé grâce à aircraftDatabaseTestGeneral()
        AircraftDatabase aircraftDatabase = new AircraftDatabase("/aircraft.zip");
        AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A8E250"));
        System.out.println(aircraftData);
        aircraftData = aircraftDatabase.get(new IcaoAddress("C07848"));
        System.out.println(aircraftData);
    }

}