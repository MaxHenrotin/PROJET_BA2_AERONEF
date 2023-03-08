package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PowerComputerTest {

    @Test
    void powerComputerThrowsNull(){
        assertThrows(NullPointerException.class,()->new PowerComputer(null,8));
    }

    @Test
    void powerComputerThrowsIllegalUnder0() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        assertThrows(IllegalArgumentException.class,()->new PowerComputer(stream,-3));
    }

    @Test
    void powerComputerThrowsIllegalIfNotModulo8() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        assertThrows(IllegalArgumentException.class,()->new PowerComputer(stream,12));
    }

    @Test
    void readBatchWorksOnNormalInput() throws IOException {
        int[] expected={73, 292, 65, 745, 98, 4226, 12244, 25722};

        int [] actual=new int[expected.length];

        InputStream stream = new FileInputStream("resources\\samples.bin");

        PowerComputer sample = new PowerComputer(stream, expected.length);

        assertEquals(expected.length,sample.readBatch(actual));

        assertArrayEquals(expected,actual);

        stream.close();
    }

    @Test
    void readBatchWorksOn160Puissances() throws IOException{ //ce test donne les 160 premières puissances qu'on a pu vérifier à l'aide d'un tableur
        int [] actual=new int[160];

        InputStream stream = new FileInputStream("resources\\samples.bin");

        PowerComputer sample = new PowerComputer(stream, actual.length);

        sample.readBatch(actual);

        //System.out.println(Arrays.toString(actual));

        stream.close();
    }

}