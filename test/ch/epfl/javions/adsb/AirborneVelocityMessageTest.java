package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AirborneVelocityMessageTest {

    @Test
    void AirbornVelocityMessageWorksOnSample() {
        int counter = 0;
        String f = "resources\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){
                AirborneVelocityMessage a = AirborneVelocityMessage.of(m);
                System.out.println(a);
                ++counter;
            }
            System.out.println(counter);
            //assertEquals( ??? , counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}