package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class PowerComputerTest {
    @Test
    void samplesDecoderWorksOnNormalInput() throws IOException {
        int[] expected={73, 292, 65, 745, 98, 4226, 12244, 25722, 36818, 23825};

        int [] actual=new int[expected.length];

        InputStream stream = new FileInputStream("samples.bin");

        PowerComputer sample = new PowerComputer(stream,10);

        assertEquals(10,sample.readBatch(actual));

        assertArrayEquals(expected,actual);
    }
}