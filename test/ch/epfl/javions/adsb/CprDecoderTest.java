package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {
    @Test
    void testValeursProf(){
        double valueLongitudeExpected = 7.476062346249819;
        double valueLatitudeExpected = 46.323349038138986;
        System.out.println("expected : "+valueLongitudeExpected+", "+valueLatitudeExpected);
        System.out.print("actual : "+CprDecoder.decodePosition(Math.scalb(111600d, -17), Math.scalb(94445d, -17), Math.scalb(108865d, -17), Math.scalb(77558d, -17), 0));
    }

}