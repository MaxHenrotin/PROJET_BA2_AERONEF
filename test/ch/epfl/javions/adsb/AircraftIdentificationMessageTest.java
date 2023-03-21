package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class AircraftIdentificationMessageTest {

    @Test
    void aircraftIdentificationWorksOnSample() {
        int counter = 0;
        String f = "resources\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){
                AircraftIdentificationMessage a = AircraftIdentificationMessage.of(m);
                if(a != null && (m.typeCode() ==1 || m.typeCode()==2 || m.typeCode()==3 ||m.typeCode()==4)) {
                    System.out.println(a);
                    ++counter;
                }
            }
            System.out.println(counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}