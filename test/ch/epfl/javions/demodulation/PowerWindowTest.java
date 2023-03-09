package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

    /**
     * ATTENTION POUR TESTER CETTE CLASSE IL FAUT CHANGER LA CONSTANTE BATCH_SIZE A 8 (2^3) dans PowerWindow
     * */
class PowerWindowTest {

    @Test
    void powerWindowConstructCorrectly() throws IOException {
        InputStream stream = new FileInputStream("resources\\samples.bin");
        assertDoesNotThrow(() -> new PowerWindow(stream, 5));
    }

    @Test
    void sizeWorks() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        assertEquals(5,window.size());
    }

    @Test
    void isFullWorks() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 30);
        assertTrue(window.isFull());
        window.advance();
        assertTrue(window.isFull());
        window.advanceBy(50);
        assertTrue(window.isFull());
        int etape = 51;
        do {
            ++etape;
            window.advance();
        } while (!window.isFull());
        System.out.println("etape : " + etape);
    }

    @Test
    void getWorks() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        System.out.println(window.get(3));
        assertEquals(745,window.get(3));
    }

    @Test
    void advanceWorks() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        window.advance();
        assertEquals(98,window.get(3));
        window.advance();
        assertEquals(4226,window.get(3));
        window.advance();
        assertEquals(12244,window.get(3));
        window.advanceBy(3);
        assertEquals(23825,window.get(3));
    }

    @Test
    void positionWorks() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        assertEquals(0,window.position());
        window.advance();
        window.advanceBy(25);
        assertEquals(26,window.position());
    }
}