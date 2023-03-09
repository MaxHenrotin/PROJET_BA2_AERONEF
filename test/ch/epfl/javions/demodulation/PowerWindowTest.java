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

    //advanceby(1196) est full et advanceby(1197) est plus full et elem 4 == 0 (window de taille 5)
    //derniere valeur vaut 585
    //Il y a 1200 valeurs et vaut 0 a partir de 1201

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
    void getWorks() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        System.out.println(window.get(3));
        assertEquals(745,window.get(3));
    }

    @Test
    void getWorksWhenNotFull() throws  IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        window.advanceBy(1196);
        assertEquals(585,window.get(4));
        window.advance();
        assertEquals(0,window.get(4));

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

    @Test
    void test() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        for(int i=0;i<10000;i++){
            window.advance();
            System.out.println(window.get(0));
        }
    }
        @Test
        void isFullWorks() throws IOException{
            InputStream stream = new FileInputStream("resources\\samples.bin");
            PowerWindow window = new PowerWindow(stream, 5);
            //sans advance
            assertTrue(window.isFull());
            //avec advance
            window.advanceBy(1196);
            assertTrue(window.isFull());
            //limite pour une window de taille 5
            window.advanceBy(1197);
            assertFalse(window.isFull());
        }

}