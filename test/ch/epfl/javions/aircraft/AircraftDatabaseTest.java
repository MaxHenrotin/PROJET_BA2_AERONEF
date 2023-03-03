package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class AircraftDatabaseTest {

    @Test
    void assertThrowsIfZipAddressIsNull() {
        assertThrows(NullPointerException.class, () -> new AircraftDatabase(null));
    }

    @Test
    void aircraftDatabaseTestAdresseIcaoOk() throws IOException {
        String dataBaseAdress = getClass().getResource("/aircraft.zip").getFile();
        dataBaseAdress = URLDecoder.decode(dataBaseAdress, UTF_8);
        AircraftDatabase aircraftDatabase = new AircraftDatabase(dataBaseAdress);
        AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A040C6"));
        assertEquals(new AircraftData(new AircraftRegistration("N115WM"), new AircraftTypeDesignator("GLST"), "GLASAIR SH-4 GlaStar", new AircraftDescription("L1P"), WakeTurbulenceCategory.LIGHT), aircraftData);
    }

    @Test
    void aircraftDatabaseTestLimite() throws IOException {
        String dataBaseAdress = getClass().getResource("/aircraft.zip").getFile();
        dataBaseAdress = URLDecoder.decode(dataBaseAdress, UTF_8);
        AircraftDatabase aircraftDatabase = new AircraftDatabase(dataBaseAdress);
        AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("FFFFFF"));
        assertNull(aircraftData);
        aircraftData = aircraftDatabase.get(new IcaoAddress("A00000"));
        assertNull(aircraftData);
    }

    @Test
    void aircraftDatabaseTestGeneral(){
        String dataBaseAdress = getClass().getResource("/aircraft.zip").getFile();
        dataBaseAdress = URLDecoder.decode(dataBaseAdress, UTF_8);
        AircraftDatabase aircraftDatabase = new AircraftDatabase(dataBaseAdress);
        int repetitionNumber = 1000;  //on teste 1000 cas aléatoires d'adresses ICAO
        do {
            String icaoAddress = "";
            for (int i = 0; i < 6; i++) {    //pour les 6 chiffres de l'adresse Icao
                int randomNumber = (int) (16 * Math.random());    //random number between 0 and 15 (tous les hexadécimaux)
                icaoAddress += Integer.toHexString(randomNumber).toUpperCase();
            }
            final String icaoAddressFinal = icaoAddress;
            assertDoesNotThrow(() -> aircraftDatabase.get(new IcaoAddress(icaoAddressFinal)));
            //AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress(icaoAddress));   //utile pour savoir les adresses qui posent problème (si il y en a)
            //System.out.println(aircraftData);   //possible car AircraftData est un enregistrement
            --repetitionNumber;
        }while(repetitionNumber > 0);
    }

    @Test
    void aircraftDatabaseTestCasSpecial() throws IOException{ //trouvé grâce à aircraftDatabaseTestGeneral()
        String dataBaseAdress = getClass().getResource("/aircraft.zip").getFile();
        dataBaseAdress = URLDecoder.decode(dataBaseAdress, UTF_8);
        AircraftDatabase aircraftDatabase = new AircraftDatabase(dataBaseAdress);
        AircraftData aircraftData = aircraftDatabase.get(new IcaoAddress("A8E250"));
        assertEquals(new AircraftData(new AircraftRegistration("N67137"), new AircraftTypeDesignator(""), "", new AircraftDescription(""), WakeTurbulenceCategory.UNKNOWN), aircraftData);
    }
}