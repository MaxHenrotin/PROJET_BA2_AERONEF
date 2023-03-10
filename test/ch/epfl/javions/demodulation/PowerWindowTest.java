package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ATTENTION : pour les tests qui suivent il peut etre interessant de changer BATCH_SIZE dans PowerWindow à 2^3 (=8)
 * ceci est fait pour que les tests fassent des changements de batch sur le fichier samples.bin (qui contient que 1200 Elements)
 */

class PowerWindowTest {
    /**
     * Pour la classe PowerWindow, on a testé les méthodes sur le fichier samples.bin
     * Dans lequel Il y a 1201 valeurs (vaut 0 à partir de l'index 1201)
     * Donc pour unewindow de taille 5 advanceby(1196) est full et advanceby(1197) est plus full et elem 4 == 0
     * (la derniere valeur vaut 585)
     */

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
    void getWorksWhenWindowNotAdvanced() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        assertEquals(745,window.get(3));
    }

    @Test
    void getWorksWhenAdvance() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);

        int[] test ={4226, 12244, 25722, 36818, 23825};
        window.advanceBy(5);
        for (int i = 0; i < test.length; i++) {
            assertEquals(test[i],window.get(i));
        }
    }


    @Test
    void getWorksWhenNotFull() throws  IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        window.advanceBy(1196);
        assertEquals(585,window.get(4));

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
    void isFullWorks() throws IOException{
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        //sans advance
        assertTrue(window.isFull());
        //avec advance
        window.advanceBy(1196);
        assertTrue(window.isFull());
        //limite pour une window de taille 5
        window.advance();
        assertFalse(window.isFull());
    }
    @Test
    void testDAffichage() throws IOException {
        InputStream stream = new FileInputStream("resources\\samples.bin");
        PowerWindow window = new PowerWindow(stream, 5);
        for (int i = 0; i < 1300; i++) {
            System.out.print(i + " : ");
            System.out.println(window.get(0));
            window.advance();
        }
    }

}