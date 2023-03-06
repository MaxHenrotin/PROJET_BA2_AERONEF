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

    public PowerComputer(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize>0 && ((batchSize%8)==0));
        Objects.requireNonNull(stream);

        this.stream=stream;
        this.batchSize=batchSize;
    }

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length==batchSize);

        short[] echantillons = new short[batchSize*8];
        SamplesDecoder sample = new SamplesDecoder(stream,batchSize*8);

        int nbrEchantillons = sample.readBatch(echantillons);



        if(nbrEchantillons%8==0) {
            for (int i = 0; i < echantillons.length;i+=8){
                batch[i/8]=calculPuissanceEchantillon(echantillons[i+7],echantillons[i+6],echantillons[i+5],echantillons[i+4],echantillons[i+3],echantillons[i+2],echantillons[i+1],echantillons[i]);
            }
        }

        return nbrEchantillons/8;
    }

    private int calculPuissanceEchantillon(int x7, int x6, int x5, int x4, int x3, int x2, int x1, int x0){
        int p1 = x6 - x4 + x2 - x0;
        int p2 = x7 - x5 + x3 - x1;
        return p1*p1 - p2*p2;
    }


}
