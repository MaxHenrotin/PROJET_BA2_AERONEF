package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

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

        InputStream stream2 = new FileInputStream("resources\\samples.bin");
        PowerWindow window2 = new PowerWindow(stream2, 5);
        window2.advanceBy(196);
        assertEquals(6893,window2.get(0));
        assertEquals(2306,window2.get(1));
        window2.advanceBy(1000);
        assertEquals(585,window2.get(4));
        window2.advanceBy(4);
        assertEquals(585,window2.get(0));
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

    private static final int BATCH_SIZE = 1 << 16;
    private static final int BATCH_SIZE_BYTES = bytesForPowerSamples(BATCH_SIZE);
    private static final int STANDARD_WINDOW_SIZE = 1200;
    private static final int BIAS = 1 << 11;

    private static int bytesForPowerSamples(int powerSamplesCount) {
        return powerSamplesCount * 2 * Short.BYTES;
    }

    @Test
    void powerWindowConstructorThrowsWithInvalidWindowSize() throws IOException {
        try (var s = InputStream.nullInputStream()) {
            assertThrows(IllegalArgumentException.class, () -> new PowerWindow(s, 0));
            assertThrows(IllegalArgumentException.class, () -> new PowerWindow(s, -1));
            assertThrows(IllegalArgumentException.class, () -> new PowerWindow(s, (1 << 16) + 1));
        }
    }

    @Test
    void powerWindowSizeReturnsWindowSize() throws IOException {
        try (var s = InputStream.nullInputStream()) {
            for (var i = 1; i <= 1 << 16; i <<= 1) {
                var w = new PowerWindow(s, i);
                assertEquals(i, w.size());
            }
        }
    }

    @Test
    void powerWindowPositionIsCorrectlyUpdatedByAdvance() throws IOException {
        var batches16 = new byte[BATCH_SIZE_BYTES * 16];
        try (var s = new ByteArrayInputStream(batches16)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            var expectedPos = 0L;

            assertEquals(expectedPos, w.position());

            w.advance();
            expectedPos += 1;
            assertEquals(expectedPos, w.position());

            w.advanceBy(BATCH_SIZE);
            expectedPos += BATCH_SIZE;
            assertEquals(expectedPos, w.position());

            w.advanceBy(BATCH_SIZE - 1);
            expectedPos += BATCH_SIZE - 1;
            assertEquals(expectedPos, w.position());

            w.advance();
            expectedPos += 1;
            assertEquals(expectedPos, w.position());
        }
    }

    @Test
    void powerWindowAdvanceByCanAdvanceOverSeveralBatches() throws IOException {
        var bytes = bytesForZeroSamples(16);

        var batchesToSkipOver = 2;
        var inBatchOffset = 37;
        var offset = batchesToSkipOver * BATCH_SIZE + inBatchOffset;
        var sampleBytes = Base64.getDecoder().decode(PowerComputerTest.SAMPLES_BIN_BASE64);
        System.arraycopy(sampleBytes, 0, bytes, bytesForPowerSamples(offset), sampleBytes.length);

        try (var s = new ByteArrayInputStream(bytes)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            w.advanceBy(inBatchOffset);
            w.advanceBy(batchesToSkipOver * BATCH_SIZE);
            var expected = Arrays.copyOfRange(PowerComputerTest.POWER_SAMPLES, 0, STANDARD_WINDOW_SIZE);
            var actual = new int[STANDARD_WINDOW_SIZE];
            for (var i = 0; i < STANDARD_WINDOW_SIZE; i += 1) actual[i] = w.get(i);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void powerWindowIsFullWorks() throws IOException {
        var twoBatchesPlusOneWindowBytes =
                bytesForPowerSamples(BATCH_SIZE * 2 + STANDARD_WINDOW_SIZE);
        var twoBatchesPlusOneWindow = new byte[twoBatchesPlusOneWindowBytes];
        try (var s = new ByteArrayInputStream(twoBatchesPlusOneWindow)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            assertTrue(w.isFull());

            w.advanceBy(BATCH_SIZE);
            assertTrue(w.isFull());

            w.advanceBy(BATCH_SIZE);
            assertTrue(w.isFull());

            w.advance();
            assertFalse(w.isFull());
        }
    }

    @Test
    void powerWindowGetWorksOnGivenSamples() throws IOException {
        try (var sampleStream = PowerComputerTest.getSamplesStream()) {
            var windowSize = 100;
            var w = new PowerWindow(sampleStream, windowSize);
            for (var offset = 0; offset < 100; offset += 1) {
                var expected = Arrays.copyOfRange(PowerComputerTest.POWER_SAMPLES, offset, offset + windowSize);
                var actual = new int[windowSize];
                for (var i = 0; i < windowSize; i += 1) actual[i] = w.get(i);
                assertArrayEquals(expected, actual);
                w.advance();
            }
        }
    }

    @Test
    void powerWindowGetWorksAcrossBatches() throws IOException {
        byte[] bytes = bytesForZeroSamples(2);
        var firstBatchSamples = STANDARD_WINDOW_SIZE / 2 - 13;
        var offset = BATCH_SIZE_BYTES - bytesForPowerSamples(firstBatchSamples);
        var sampleBytes = Base64.getDecoder().decode(PowerComputerTest.SAMPLES_BIN_BASE64);
        System.arraycopy(sampleBytes, 0, bytes, offset, sampleBytes.length);
        try (var s = new ByteArrayInputStream(bytes)) {
            var w = new PowerWindow(s, STANDARD_WINDOW_SIZE);
            w.advanceBy(BATCH_SIZE - firstBatchSamples);
            for (int i = 0; i < STANDARD_WINDOW_SIZE; i += 1)
                assertEquals(PowerComputerTest.POWER_SAMPLES[i], w.get(i));
        }
    }

    private static byte[] bytesForZeroSamples(int batchesCount) {
        var bytes = new byte[BATCH_SIZE_BYTES * batchesCount];

        var msbBias = BIAS >> Byte.SIZE;
        var lsbBias = BIAS & ((1 << Byte.SIZE) - 1);
        for (var i = 0; i < bytes.length; i += 2) {
            bytes[i] = (byte) lsbBias;
            bytes[i + 1] = (byte) msbBias;
        }
        return bytes;
    }

}