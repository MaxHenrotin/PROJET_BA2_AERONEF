package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Représente un «décodeur d'échantillons», donc un objet capable de transformer les octets provenant de la AirSpy en des échantillons de 12 bits signés
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class SamplesDecoder {
    private final int batchSize;

    private InputStream stream;

    private byte [] bytes;

    public SamplesDecoder(InputStream stream, int batchSize) throws IOException {
        Preconditions.checkArgument(batchSize>0);
        Objects.requireNonNull(stream);

        this.batchSize=batchSize;
        this.stream=stream;
        bytes=new byte[batchSize*2];
    }

    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batchSize == batch.length);
        bytes = stream.readNBytes(batchSize*2);

        for (int i=0; i<bytes.length ; i += 2){
            batch[i/2]= (short) (calculEchantillon((short) (bytes[i+1]&0xFF), (short) (bytes[i]&0xFF)) - 2048);
        }

        return bytes.length/2;
    }

    private short calculEchantillon(short fort,short faible){
        return (short)((fort << Byte.SIZE) | faible);
    }
}
