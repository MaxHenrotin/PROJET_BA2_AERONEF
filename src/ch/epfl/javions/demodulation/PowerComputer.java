package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

public class PowerComputer {
    private InputStream stream;

    private final int batchSize;

    private int indexAncienEchatillon;

    private int[] tabCirculaire = new int[8];

    private short[] echantillons;

    public PowerComputer(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize>0 && ((batchSize%8)==0));
        Objects.requireNonNull(stream);

        this.stream=stream;
        this.batchSize=batchSize;
        echantillons=new short[batchSize*2];
    }

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);

        SamplesDecoder sample = new SamplesDecoder(stream,batchSize*2);

        int nbrEchantillons = sample.readBatch(echantillons);

        return nbrEchantillons/8;
    }

    private int calculPuissanceEchantillon(int x7, int x6, int x5, int x4, int x3, int x2, int x1, int x0){
        int p1 = x6 - x4 + x2 - x0;
        int p2 = x7 - x5 + x3 - x1;
        return p1*p1 - p2*p2;
    }


}
