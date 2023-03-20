package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {

    private double norme = Math.scalb(1,-17);
    @Test
    void testValeursProf(){
        double valueLongitudeExpected = 7.476062;
        double valueLatitudeExpected = 46.323349;
        System.out.println("expected : "+valueLongitudeExpected+", "+valueLatitudeExpected);
        System.out.print("actual : "+CprDecoder.decodePosition(111600 * norme, 94445 * norme, 108865*norme, 77558*norme, 0));
    }

}