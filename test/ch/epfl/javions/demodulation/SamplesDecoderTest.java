package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SamplesDecoderTest {
    @Test
    void readBatchWorksOnNormalInput() throws IOException {
        short[] expected={-3, 8, -9, -8, -5, -8, -12, -16, -23, -9};
        int batchSize=10;

        short [] actual=new short[batchSize];

        InputStream stream = new FileInputStream("resources\\samples.bin");

        SamplesDecoder sample = new SamplesDecoder(stream,batchSize);

        assertEquals(batchSize,sample.readBatch(actual));

        assertArrayEquals(expected,actual);

        stream.close();
    }

    @Test
    void samplesDecoderThrowNull(){
        assertThrows(NullPointerException.class,()->new SamplesDecoder(null,10));
    }

    @Test
    void samplesDecoderThrowIllegal() throws IOException {
        InputStream stream = new FileInputStream("resources\\samples.bin");
        assertThrows(IllegalArgumentException.class,()->new SamplesDecoder(stream,-5));
    }

    @Test
    void readBatchThrowIllegal() throws IOException{
        int batchSize=10;

        short [] actual=new short[batchSize+1];

        InputStream stream = new FileInputStream("resources\\samples.bin");

        SamplesDecoder sample = new SamplesDecoder(stream,batchSize);

        assertThrows(IllegalArgumentException.class,()-> sample.readBatch(actual));
    }

    @Test
    void readBatchWorksOnSpecialCases() throws IOException {
        String fileName="samplesDecoderTest.bin";
        OutputStream fileTest = new FileOutputStream(fileName);

        byte[] bytes = {0, 0, -1, -1, 1, 1};
        fileTest.write(bytes);
        fileTest.close();

        InputStream file = new FileInputStream(fileName);
        int batchSize = bytes.length/2;

        short[] expected={-2048,-2049,-1791};
        short [] actual=new short[batchSize];
        SamplesDecoder decoder = new SamplesDecoder(file,batchSize);

        assertEquals(batchSize,decoder.readBatch(actual));

        assertArrayEquals(expected,actual);
        file.close();

    }

    @Test
    void readBatchWorksOnOddTabSize() throws IOException {
        String fileName="samplesDecoderTest.bin";
        OutputStream fileTest = new FileOutputStream(fileName);

        byte[] bytes = {0, 0, -1, -1, 1, 1,7};
        fileTest.write(bytes);
        fileTest.close();

        InputStream file = new FileInputStream(fileName);
        int batchSize = 2;

        short[] expected={-2048,-2049};
        short [] actual=new short[batchSize];
        SamplesDecoder decoder = new SamplesDecoder(file,batchSize);

        assertEquals(batchSize,decoder.readBatch(actual));

        assertArrayEquals(expected,actual);

        file.close();

    }

}