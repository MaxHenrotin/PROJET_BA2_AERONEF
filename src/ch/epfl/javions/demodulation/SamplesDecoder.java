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

    public SamplesDecoder(InputStream stream, int batchSize) throws IOException {
        Preconditions.checkArgument(batchSize>0);
        Objects.requireNonNull(stream);

        this.batchSize=batchSize;
        this.stream=stream;
    }

    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batchSize==batch.length);
        byte[] bytes = stream.readNBytes(batchSize*2);

        for (int i=0; i<bytes.length ; i += 2){
            batch[i/2]= calculEchantillon(bytes[i+1],bytes[i]);
        }

        return bytes.length/2;
    }

    private short calculEchantillon(byte fort,byte faible){
        return (short)((fort << Byte.SIZE) & (faible));
    }
}
