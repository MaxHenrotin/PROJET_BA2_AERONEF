package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionMessageTest {

    @Test
    void AirbornPositionMessageWorksOnSample() {
        int counter = 0;
        String f = "resources\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){
                AirbornePositionMessage a = AirbornePositionMessage.of(m);
                if(a != null && ((m.typeCode() >=9 && m.typeCode() <=18) || (m.typeCode() >= 20 && m.typeCode() <= 22))) {
                    System.out.println(a);
                    ++counter;
                }
            }
            System.out.println("On en veut 137 avec les conditions sur typeCode et 311 sans ! Et il y en a ici : " + counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}