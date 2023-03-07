package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

public class PowerComputer {
    private InputStream stream;

    private final int batchSize;

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
        int currentIndex;

        for (int i=0;i < nbrEchantillons;i+=2){
            currentIndex = i%8;

            tabCirculaire[currentIndex] = echantillons[i];
            tabCirculaire[currentIndex+1] = echantillons[i+1];

            batch[i/2]=calculPuissanceEchantillon(tabCirculaire,currentIndex);
        }


        return nbrEchantillons/2;
    }

    private int calculPuissanceEchantillon(int[] tab, int lastIndex){
        int lastIndex0=lastIndex;
        int lastIndex1=lastIndex+1;

        int p1 = tab[(lastIndex1+2)%8] - tab[(lastIndex1+4)%8] + tab[(lastIndex1+6)%8] - tab[lastIndex1];
        int p2 = tab[(lastIndex0+2)%8] - tab[(lastIndex0+4)%8] + tab[(lastIndex0+6)%8] - tab[lastIndex0];

        return p1*p1 + p2*p2;
    }


}
