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

    private AircraftDatabase getDatabase() {
        // Try to get the database from the resources
        var aircraftResourceUrl = getClass().getResource("/aircraft.zip");
        if (aircraftResourceUrl != null)
            return new AircraftDatabase(URLDecoder.decode(aircraftResourceUrl.getFile(), UTF_8));

        // Try to get the database from the JAVIONS_AIRCRAFT_DATABASE environment variable
        // (only meant to simplify testing of several projects with a single database)
        var aircraftFileName = System.getenv("JAVIONS_AIRCRAFT_DATABASE");
        if (aircraftFileName != null)
            return new AircraftDatabase(aircraftFileName);

        throw new Error("Could not find aircraft database");
    }

    @Test
    void aircraftDatabaseGetReturnsNullWhenAddressDoesNotExist() throws IOException {
        var aircraftDatabase = getDatabase();
        assertNull(aircraftDatabase.get(new IcaoAddress("123456")));
    }

    @Test
    void aircraftDatabaseGetWorksWithFirstLineOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("0086AB"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("ZS-CNA"), aircraftData.registration());
    }

    @Test
    void aircraftDatabaseGetWorksWithLastLineOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("E808C0"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("CC-DAW"), aircraftData.registration());
    }

    @Test
    void aircraftDatabaseGetWorksWithAddressGreaterThanLastOneOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("FFFF01"));
        assertNull(aircraftData);
    }

    @Test
    void aircraftDatabaseGetReturnsCorrectData() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("4B1805"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("HB-JCN"), aircraftData.registration());
        assertEquals(new AircraftTypeDesignator("BCS3"), aircraftData.typeDesignator());
        assertEquals("AIRBUS A220-300", aircraftData.model());
        assertEquals(new AircraftDescription("L2J"), aircraftData.description());
        assertEquals(WakeTurbulenceCategory.MEDIUM, aircraftData.wakeTurbulenceCategory());
    }

    //
    @Test
    void aircraftDatabaseGetWorksWithEmptyColumns() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("AAAAAA"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("N787BK"), aircraftData.registration());
        assertEquals(new AircraftTypeDesignator(""), aircraftData.typeDesignator());
        assertEquals("", aircraftData.model());
        assertEquals(new AircraftDescription(""), aircraftData.description());
        assertEquals(WakeTurbulenceCategory.UNKNOWN, aircraftData.wakeTurbulenceCategory());
    }
}