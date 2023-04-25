package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.collections.ObservableSet;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

class AircraftStateManagerTest {

    @Test
    public void testDeBase(){
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("resources\\messages_20230318_0915.bin")))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                System.out.printf("%13d: %s\n", timeStampNs, message);
            }
        }
        catch (EOFException e) { /* nothing to do */ }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
    }

    @Test
    public void testSurDataBase(){

        String dataBaseAdress = getClass().getResource("/aircraft.zip").getFile();
        dataBaseAdress = URLDecoder.decode(dataBaseAdress, UTF_8);
        AircraftDatabase aircraftDatabase = new AircraftDatabase(dataBaseAdress);

        int counterToPurge = 0;

        try (DataInputStream stream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("resources\\messages_20230318_0915.bin")))){

            byte[] bytes = new byte[RawMessage.LENGTH];
            AircraftStateManager aircraftStateManager = new AircraftStateManager(aircraftDatabase);

            //System.out.println("OACI  Indicatif  Immat.   Modèle                      Longitude   Latitude   Alt.   Vit.");
            //System.out.println("----------------------------------------------------------------------------------------");

            while (true) {
                long timeStampNs = stream.readLong();
                int bytesRead = stream.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                //ByteString message = new ByteString(bytes);
                RawMessage message = RawMessage.of(timeStampNs,bytes);

                for (int i = 0; i < 20; i++) {


                    if (message != null) {

                        aircraftStateManager.updateWithMessage(MessageParser.parse(message));

                        List<ObservableAircraftState> states = new ArrayList<>(aircraftStateManager.states());
                        states.sort(new AddressComparator());

                        for (ObservableAircraftState s : states) {
                            System.out.println(
                                    (s.getIcaoAddress() == null ? "\t" : s.getIcaoAddress().string()) + "\t" +
                                            (s.getCallSign() == null ? "\t" : s.getCallSign().string()) + "\t" +
                                            (s.getAircraftData() == null ? "\t" : s.getAircraftData().registration().string()) + "\t" +
                                            (s.getAircraftData() == null ? "\t" : s.getAircraftData().model()) + "\t" +
                                            (s.getPosition() == null ? "\t" : s.getPosition().toString()) + "\t" +
                                            (Math.rint(s.getAltitude())) + "\t" +
                                            Math.rint(s.getVelocity() * 3.6));
                        }
                    }
                }

                aircraftStateManager.purge();

            }
        }
        catch (EOFException e) { /* nothing to do */ }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
    }


    private static class AddressComparator
            implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1,
                           ObservableAircraftState o2) {
            String s1 = o1.getIcaoAddress().string();
            String s2 = o2.getIcaoAddress().string();
            return s1.compareTo(s2);
        }
    }

    @Test
    public static void main(String[] args) throws URISyntaxException {

        URL res = AircraftStateManagerTest.class.getClassLoader().getResource("aircraft.zip");
        File fileAircraftZip = Paths.get(res.toURI()).toFile();
        String absolutePathAircraft = fileAircraftZip.getAbsolutePath();

        AircraftStateManager manager = new AircraftStateManager(new AircraftDatabase(absolutePathAircraft));
        String alignFormat = "|%-6s|%-14s|%-13s|%-20.20s|%-20.5f|%-20.5f|%-10.0f|%-10.0f|%-3s| ";
        System.out.format("+------+--------------+-------------+--------------------+--------------------+--------------------+----------+----------+---+%n");
        System.out.format("| OACI |  Indicatif   |   Immat.    |       Modèle       |      Longitude     |      Latitude      |   Alt.   |   Vit.   |   |%n");
        System.out.format("+------+--------------+-------------+--------------------+--------------------+--------------------+----------+----------+---+%n");
        final AddressComparator comparator = new AddressComparator();

        URL ulrres = AircraftStateManagerTest.class.getClassLoader().getResource("messages_20230318_0915.bin");
        File file = Paths.get(ulrres.toURI()).toFile();
        String absolutePath = file.getAbsolutePath();

        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(absolutePath)))){

            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                RawMessage message = RawMessage.of(timeStampNs,bytes);
                manager.updateWithMessage(MessageParser.parse(message));
                System.out.println("\u001B[" + "2J"); // clear screen
                System.out.format("+------+--------------+-------------+--------------------+--------------------+--------------------+----------+----------+---+%n");
                System.out.format("| OACI |  Indicatif   |   Immat.    |       Modèle       |      Longitude     |      Latitude      |   Alt.   |   Vit.   |   |%n");
                System.out.format("+------+--------------+-------------+--------------------+--------------------+--------------------+----------+----------+---+%n");
                Set<ObservableAircraftState> knownPos = manager.states();
                List<ObservableAircraftState> knownPosRound2 = new ArrayList<>(knownPos);
                knownPosRound2.sort(comparator);
                for (ObservableAircraftState state : knownPosRound2) {
                    if ( !(state.getAircraftData() == null) ){
                        String callsign;
                        if (state.getCallSign() == null){
                            callsign = "Unknown";
                        }
                        else {
                            callsign = state.getCallSign().string();
                        }
                        System.out.format(alignFormat, state.getIcaoAddress().string(), callsign,
                                state.getAircraftData().registration().string(),  state.getAircraftData().model(),
                                Units.convert(state.getPosition().longitude(),Units.Angle.RADIAN,Units.Angle.DEGREE),
                                Units.convert(state.getPosition().latitude(),Units.Angle.RADIAN,Units.Angle.DEGREE),
                                state.getAltitude(),
                                (state.getVelocity()*3.6), state.getTrackOrHeading());
                        System.out.println("amog");
                    }
                }


            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}