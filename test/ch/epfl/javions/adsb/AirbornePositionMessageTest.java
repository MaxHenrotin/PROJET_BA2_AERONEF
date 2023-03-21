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
        String[] rawQ0String = {"8D39203559B225F07550ADBE328F","8DAE02C85864A5F5DD4975A1A3F5"};


    }

}