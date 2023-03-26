package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class MessageParserTest {

    @Test
    void MessageParserWorksOnSample(){
        int positionCounter = 0;
        int velocityCounter = 0;
        int identificationCounter = 0;
        int nullCounter = 0;

        String f = "resources\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){

                Message a = MessageParser.parse(m);

                System.out.println(a);

                if(a instanceof AirbornePositionMessage){ ++positionCounter; }
                if(a instanceof AirborneVelocityMessage){ ++velocityCounter; }
                if(a instanceof AircraftIdentificationMessage){ ++identificationCounter; }
                if(a == null){ ++nullCounter; }

            }

            System.out.println("positionCounter = " + positionCounter);
            System.out.println("velocityCounter = " + velocityCounter);
            System.out.println("identificationCounter = " + identificationCounter);
            System.out.println("nullCounter = " + nullCounter);

            int total = positionCounter + velocityCounter + identificationCounter + nullCounter;
            System.out.println("total = " + total);

            assertEquals(384, total);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}