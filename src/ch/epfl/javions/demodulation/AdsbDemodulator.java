package ch.epfl.javions.demodulation;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classe représentant un démodulateur de messages ADS-B
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public final class AdsbDemodulator {

    private final PowerWindow window;

    private final static int WINDOW_SIZE = 1200;    //en unité de temps de 0.1µs (=unité de temps de l'échantillonnage)

    private final static int IMPULSION_TIME = 5; //en unité de temps de 0.1µs (=unité de temps de l'échantillonnage)

    /**
     * Constructeur de la classe retournant un démodulateur obtenant les octets contenant les échantillons du flot passé en argument
     * Concrètement on construit un objet de type PowerWindow représentant la fenêtre de 1200 échantillons de puissance, utilisée pour rechercher les messages,
     * celle-ci est liée au flot d'échantillons reçu de la radio
     * @param samplesStream : flot contenant les informations reçues de la radio
     * @throws IOException si une erreur d'entrée/sortie se produit lors de la création de la PowerWindow
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        window = new PowerWindow(samplesStream, WINDOW_SIZE);
    }

    /**
     * Retourne le prochain message ADS-B du flot d'échantillons passé au constructeur, ou null s'il n'y en a plus (c.-à-d. que la fin du flot d'échantillons a été atteinte)
     * L'horodatage des messages retournés par nextMessage utilise l'instant correspondant au tout premier échantillon de puissance comme origine du temps.
     * @return le prochain message ADS-B du flot d'échantillons passé au constructeur, ou null s'il n'y en a plus
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public RawMessage nextMessage() throws IOException{
        RawMessage rawMessage;
        byte[] bytes;

        int sommePorteuseEmise0;
        int sommePorteuseEmise1;
        int sommePorteuseEmise2;
        int sommePorteuseNonEmise;

        //ici le nom de la variable ne correspond pas mais il va etre reafecté juste apres pour ajuster celà
        sommePorteuseEmise1 = calculSommePorteuse(0);  //tout premier calcul de somme porteuse
        sommePorteuseEmise2 = calculSommePorteuse(1);  //2e calcul de somme porteuse

        while (window.isFull()){

            sommePorteuseEmise0 = sommePorteuseEmise1;  //pour optimiser le temps et eviter de recalculer les sommes porteuses déjà calculées avant
            sommePorteuseEmise1 = sommePorteuseEmise2;
            sommePorteuseEmise2 = calculSommePorteuse(2);
            sommePorteuseNonEmise = calculSommeNonPorteuse();

            if ((sommePorteuseEmise0 < sommePorteuseEmise1) && (sommePorteuseEmise1 > sommePorteuseEmise2) && (sommePorteuseEmise1 >= 2*sommePorteuseNonEmise)){
                window.advance();

                bytes = new byte[RawMessage.LENGTH];

                for (int i = 0; i < Byte.SIZE; i++) {
                    bytes[0] = (byte) ((bytes[0]<<1) | calculBit(i));
                }

                if(RawMessage.size(bytes[0]) == RawMessage.LENGTH){

                    for (int i = Byte.SIZE ; i < RawMessage.LENGTH * Byte.SIZE ; i++) {
                        bytes[i/Byte.SIZE] = (byte) ((bytes[i/Byte.SIZE]<<1) | calculBit(i));
                    }

                    rawMessage = RawMessage.of((long) (window.position()*(0.1*Units.KILO)),bytes);   //Units.KILO/10 car on travaille en echantillonage de 0.1µs ce qui représente 100 nanosecondes
                    if (rawMessage != null) {
                        window.advanceBy(WINDOW_SIZE);
                        return rawMessage;
                    }

                }
            }
            window.advance();
        }
        return null;
    }

    /**
     * Calcule selon la formule donnée dans le sujet la ième somme des puissances des impulsions porteuses
     * @param i : index de la somme porteuse à calculer
     * @return : la somme des puissances porteuses voulue
     */
    private int calculSommePorteuse(int i){
        return window.get(i)+ window.get(i + 2*IMPULSION_TIME)+window.get(i + 7*IMPULSION_TIME)+window.get(i + 9*IMPULSION_TIME);
    }

    /**
     * Calcule selon la formule donnée dans le sujet la somme des puissances des impulsions non porteuses
     * @return la somme des puisances non porteuses
     */
    private int calculSommeNonPorteuse(){
        return window.get(1 + IMPULSION_TIME)+ window.get(1 + 3*IMPULSION_TIME)+window.get(1 + 4*IMPULSION_TIME)+window.get(1 + 5*IMPULSION_TIME)+window.get(1 + 6*IMPULSION_TIME)+window.get(1 + 8*IMPULSION_TIME);
    }

    /**
     * Détermine le bit à l'index i dans le message selon la formule donnée dans le sujet
     * @param i : index du bit à calculer dans le message
     * @return le bit calculé
     */
    private byte calculBit(int i){
        return (byte) ((window.get(16*IMPULSION_TIME + 2*IMPULSION_TIME * i) < window.get(17*IMPULSION_TIME + 2*IMPULSION_TIME * i)) ? 0 : 1);
    }
}

