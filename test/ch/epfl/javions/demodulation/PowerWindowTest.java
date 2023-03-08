package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class PowerWindowTest {

    @Test
    void powerWindowConstructCorrectly() throws IOException {
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 10000);

        System.out.println(window.size());
        System.out.println(window.position());
        System.out.println(window.isFull());
        System.out.println(window.get(3));
        window.advance();
        window.advanceBy(5);
        System.out.println(window.position());
        System.out.println(window.get(3));
    }
}