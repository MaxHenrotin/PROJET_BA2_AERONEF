package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PowerComputerTest {
    @Test
    void powerComputerWorksOnNormalInput() throws IOException {
        int[] expected={73, 292, 65, 745, 98, 4226, 12244, 25722, 36818, 23825};

        int [] actual=new int[expected.length*4];

        InputStream stream = new FileInputStream("C:\\Users\\Petit\\Documents\\BA2 Prog\\ProjetBA2Aeronef\\resources\\samples.bin");

        PowerComputer sample = new PowerComputer(stream, expected.length*4);

        assertEquals(10,sample.readBatch(actual));

        sample.readBatch(actual);

        System.out.println(Arrays.toString(actual));

        assertArrayEquals(expected,actual);

        stream.close();
    }
}