package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Représente un décodeur d'échantillons, donc un objet capable de transformer les octets provenant de la AirSpy en des échantillons de 12 bits signés
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class SamplesDecoder {

    private final int batchSize; //stocke la taille des lots d'échantillons

    private InputStream stream;

    private byte [] bytes; //stocke les bytes reçus par le flot

    /**
     * "Construit" un décodeur d'échantillons utilisant le flot d'entrée donné pour obtenir les octets de la radio AirSpy et produisant les échantillons par lots de taille donnée
     * @param stream : flot de bytes nécessaires pour construire les échantillons
     * @param batchSize : taille des lots
     * @throws IOException : si le flot est null
     * @throws IllegalArgumentException : si la taille des lots n'est pas supérieure à 0
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize>0);
        Objects.requireNonNull(stream);

        this.batchSize=batchSize;
        this.stream=stream;
        bytes=new byte[batchSize*2]; //besoin de 2 octets pour obtenir un échnatillon
    }

    /**
     * Lit depuis le flot passé au constructeur le nombre d'octets correspondant à un lot,
     * puis convertit ces octets en échantillons signés, qui sont placés dans le tableau passé en argument;
     * le nombre d'échantillons effectivement converti est retourné, et il est toujours égal à la taille du lot sauf lorsque la fin du flot a été atteinte avant d'avoir pu lire assez d'octets,
     * auquel cas il est égal au nombre d'octets lus divisé par deux, arrondi vers le bas
     * @param batch : tableau qui va contenir les échantillons
     * @return le nombre d'échantillons prodiuts
     * @throws IOException : si le flot est illisible
     * @throws IllegalArgumentException: si la taille du tableau passé en argument est différente de la taille d'un lot
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batchSize == batch.length);
        bytes = stream.readNBytes(batchSize*2);

        for (int i=0; i < bytes.length ; i += 2){
            batch[i/2]= (short) (calculEchantillon((short) (bytes[i+1]&0xFF), (short) (bytes[i]&0xFF)) - 2048);
        }

        return bytes.length/2;
    }

    //Manipule les 2 octets lus pour en extraire l'échantillon correspondant
    private short calculEchantillon(short fort,short faible){
        return (short)((fort << Byte.SIZE) | faible);
    }
}