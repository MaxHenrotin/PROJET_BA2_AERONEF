package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AdsbDemodulatorTest {

    @Test   //test du prof
    void testVitesse() {
        int counter = 0;
        String f = "resources\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){
                System.out.println(m);
                System.out.println(m.icaoAddress());
                ++counter;
            }
            assertEquals(384, counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}