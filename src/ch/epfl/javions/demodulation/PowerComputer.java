package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Représente un calculateur de puissance, soit un objet capable de calculer les échantillons de puissance du signal
 * à partir des échantillons signés produits par un décodeur d'échantillons
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public class PowerComputer {

    //===================================== Attributs privées statiques ================================================

    private final static int NBR_ECHANTILLONS_MEMOIRE = 8;

    //On a besoin de 2 échantillons pour calculer une puissance
    private final static int NBR_ECHANTILLONS_PAR_PUISSANCE = 2;


    //===================================== Attributs privées ==========================================================

    private final int batchSize; //taille des lots de puissance
    private final SamplesDecoder sample; //décodeur d'échantillons
    private final int[] tabCirculaire = new int[8]; //stocke les 8 derniers échantillons
    private final short[] echantillons; //stocke les échantillons


    //===================================== Méthodes privées ===========================================================

    //Utilise la formule de calcul de puissance donnée dans l'énoncé
    private int calculPuissanceEchantillon(int[] tab, int lastIndex){
        int lastIndex0 = lastIndex;
        int lastIndex1 = lastIndex + 1;

        int p1 = tab[(lastIndex1+2) % NBR_ECHANTILLONS_MEMOIRE] - tab[(lastIndex1+4) % NBR_ECHANTILLONS_MEMOIRE]
                    + tab[(lastIndex1+6) % NBR_ECHANTILLONS_MEMOIRE] - tab[lastIndex1];
        int p2 = tab[(lastIndex0+2) % NBR_ECHANTILLONS_MEMOIRE] - tab[(lastIndex0+4) % NBR_ECHANTILLONS_MEMOIRE]
                    + tab[(lastIndex0+6) % NBR_ECHANTILLONS_MEMOIRE] - tab[lastIndex0];

        return p1*p1 + p2*p2;
    }

    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur retournant un calculateur de puissance utilisant le flot d'entrée donné pour obtenir
     * les octets de la radio AirSpy et produisant des échantillons de puissance par lots de taille donnée
     *
     * @param stream : flot de bytes contenant les echantillons
     * @param batchSize : taille d'un lot
     * @throws IllegalArgumentException : si la taille d'un lot est inférieur à 0 ou si ce n'est pas un multiple de 8
     * @throws NullPointerException : si le flot est null
     */
    public PowerComputer(InputStream stream, int batchSize){
        Preconditions.checkArgument((batchSize > 0) && ((batchSize % NBR_ECHANTILLONS_MEMOIRE) == 0));
        Objects.requireNonNull(stream);

        this.batchSize = batchSize;

        sample = new SamplesDecoder(stream,batchSize * NBR_ECHANTILLONS_PAR_PUISSANCE);
        echantillons=new short[batchSize * NBR_ECHANTILLONS_PAR_PUISSANCE];
    }

    /**
     * Lit depuis le décodeur d'échantillons le nombre d'échantillons nécessaire au calcul d'un lot
     * d'échantillons de puissance, puis les calcule (au moyen de la formule donnée à la §2.4.6 de l'énoncé)
     * et les place dans le tableau passé en argument
     *
     * @param batch : tableau qui va contenir les puissances
     * @return le nombre d'échantillons de puissance placés dans le tableau
     * @throws IOException : si le flot est illisible
     * @throws IllegalArgumentException : si la taille de batch n'est pas égale à la taille d'un lot
     */

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);

        int nbrEchantillons = sample.readBatch(echantillons);
        int currentIndex;


        for (int i = 0 ; i < nbrEchantillons ; i+=NBR_ECHANTILLONS_PAR_PUISSANCE){
            currentIndex = i % NBR_ECHANTILLONS_MEMOIRE;

            tabCirculaire[currentIndex] = echantillons[i];
            tabCirculaire[currentIndex + 1] = echantillons[i + 1];

            batch[i/NBR_ECHANTILLONS_PAR_PUISSANCE] = calculPuissanceEchantillon(tabCirculaire,currentIndex);
        }
        return nbrEchantillons/NBR_ECHANTILLONS_PAR_PUISSANCE;
    }
}