package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {
    @Test
    void testValeursProf() {
        String expected = "(7.476062346249819°, 46.323349038138986°)";
        GeoPos actual = CprDecoder.decodePosition(Math.scalb(111600d, -17), Math.scalb(94445d, -17), Math.scalb(108865d, -17), Math.scalb(77558d, -17), 0);
        if (actual != null) {
            assertEquals(expected, actual.toString());
        }
    }

    @Test
    void testValeursInternet() {

       GeoPos actual = CprDecoder.decodePosition(Math.scalb(39846d, -17), Math.scalb(92095d, -17), Math.scalb(125818d, -17), Math.scalb(88385d, -17), 0);
        System.out.println(actual);
/*
        GeoPos actual = CprDecoder.decodePosition(0.3919
                ,0.7095,0.3829,0.5658,0);
        System.out.println(actual);*/
/*
        GeoPos actual = CprDecoder.decodePosition(Math.scalb(50d, -17), Math.scalb(10d, -17), Math.scalb(53d, -17), Math.scalb(50000000d, -17), 0);
        System.out.println(actual);*/

    }

    @Test
    void testValeurTelegram(){
        GeoPos pos = CprDecoder.decodePosition(0.62,0.42,0.6200000000000000001,0.4200000000000000001,0);
        System.out.println(pos);
    }
}