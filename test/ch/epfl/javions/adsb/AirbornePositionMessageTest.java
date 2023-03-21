package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionMessageTest {

    @Test
    void AirbornPositionMessageWorksOnSampleWithValidTypeCode() {
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
            assertEquals(137, counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void AirbornPositionMessageWorksOnSampleWithoutValidTypeCode() {
        int counter = 0;
        String f = "resources\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null){
                AirbornePositionMessage a = AirbornePositionMessage.of(m);
                if(a != null) {
                    System.out.println(a);
                    ++counter;
                }
            }
            assertEquals(311, counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAltitudeQ0(){

        double delta = 1e-8; //marge d'erreur parce que l'on manipule des double

        String rawMessage1String = "8D39203559B225F07550ADBE328F";  // altitude : environ 3474.72 m
        ByteString rawMessage1ByteString = ByteString.ofHexadecimalString(rawMessage1String);
        RawMessage rawMessage1 = new RawMessage(0, rawMessage1ByteString);
        assertEquals(3474.72, AirbornePositionMessage.of(rawMessage1).altitude(),delta);

        String rawMessage2String = "8DAE02C85864A5F5DD4975A1A3F5";  // altitude : environ 7315.20 m
        ByteString rawMessage2ByteString = ByteString.ofHexadecimalString(rawMessage2String);
        RawMessage rawMessage2 = new RawMessage(1, rawMessage2ByteString);
        assertEquals(7315.20, AirbornePositionMessage.of(rawMessage2).altitude(),delta);
    }

}