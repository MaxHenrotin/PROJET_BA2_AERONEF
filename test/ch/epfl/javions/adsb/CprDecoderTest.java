package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {
    @Test
    void testValeursProf(){
        String expected = "(7.476062346249819°, 46.323349038138986°)";
        GeoPos actual = CprDecoder.decodePosition(Math.scalb(111600d, -17), Math.scalb(94445d, -17), Math.scalb(108865d, -17), Math.scalb(77558d, -17), 0);
        if (actual != null) {
            assertEquals(expected, actual.toString() );
        }
    }
}