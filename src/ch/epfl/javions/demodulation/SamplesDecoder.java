package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Représente un décodeur d'échantillons,
 * donc un objet capable de transformer les octets provenant de la AirSpy en des échantillons de 12 bits signés
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class SamplesDecoder {

    //===================================== Attributs privées statiques ================================================

    private static final int ECHANTILLON_SIZE = 12; //en bit (ce que la AIRSPY nous donne)
    private static final int NBR_BYTE_PAR_ECHANTILLON = 2;  //On a besoin de 2 octets pour obtenir un échnatillon

    private static final int TO_UNSIGNED_INT = 0xFF; //permet d'éviter les problèmes de signes


    //===================================== Attributs privées ==========================================================

    private final int batchSize; //stocke la taille des lots d'échantillons

    private final InputStream stream;

    private byte [] bytes; //stocke les bytes reçus par le flot


    //===================================== Méthodes privées ===========================================================

    //Manipule les 2 octets lus pour en extraire l'échantillon correspondant
    private short calculEchantillon(short fort,short faible){
        return (short)((fort << Byte.SIZE) | faible);
    }


    //===================================== Méthodes publiques =========================================================

    /**
     * "Construit" un décodeur d'échantillons utilisant le flot d'entrée donné pour obtenir
     * les octets de la radio AirSpy et produisant les échantillons par lots de taille donnée
     *
     * @param stream : flot de bytes nécessaires pour construire les échantillons
     * @param batchSize : taille des lots
     * @throws IllegalArgumentException : si la taille des lots n'est pas supérieure à 0
     * @throws NullPointerException : si le stream passé en argument est null
     */
    public SamplesDecoder(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize>0);
        Objects.requireNonNull(stream);

        this.batchSize=batchSize;
        this.stream=stream;
        bytes=new byte[batchSize*NBR_BYTE_PAR_ECHANTILLON];
    }

    /**
     * Lit depuis le flot passé au constructeur le nombre d'octets correspondant à un lot,
     * puis convertit ces octets en échantillons signés, qui sont placés dans le tableau passé en argument;
     * le nombre d'échantillons effectivement converti est retourné, et il est toujours égal à la taille du lot
     * sauf lorsque la fin du flot a été atteinte avant d'avoir pu lire assez d'octets,
     * auquel cas il est égal au nombre d'octets lus divisé par deux, arrondi vers le bas
     *
     * @param batch : tableau qui va contenir les échantillons
     * @return le nombre d'échantillons prodiuts
     * @throws IOException : si le flot est illisible
     * @throws IllegalArgumentException: si la taille du tableau passé en argument est différente de la taille d'un lot
     */
    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batchSize == batch.length);
        bytes = stream.readNBytes(batchSize * NBR_BYTE_PAR_ECHANTILLON);

        for (int i=0; i < bytes.length ; i += NBR_BYTE_PAR_ECHANTILLON){

            //(1<<ECHANTILLON_SIZE) est equivalent à Math.scalb(1,ECHANTILLON_SIZE-1)
            batch[i/2] = (short) (calculEchantillon((short) (bytes[i+1] & TO_UNSIGNED_INT),
                                                    (short) (bytes[i] & TO_UNSIGNED_INT)) - (1<<ECHANTILLON_SIZE-1));

        }
        return bytes.length/NBR_BYTE_PAR_ECHANTILLON;
    }
}