package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


class SamplesDecoderTest {

    @Test
    void samplesDecoderWorksOnNormalInput() throws IOException {
        short[] expected={-3, 8, -9, -8, -5, -8, -12, -16, -23, -9};

        short [] actual=new short[expected.length];

        InputStream stream = new FileInputStream("resources\\samples.bin");

        SamplesDecoder sample = new SamplesDecoder(stream,10);

        assertEquals(10,sample.readBatch(actual));

        assertArrayEquals(expected,actual);

        stream.close();
    }

}