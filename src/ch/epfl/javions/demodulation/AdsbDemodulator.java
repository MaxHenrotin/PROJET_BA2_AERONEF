package ch.epfl.javions.demodulation;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {

    private PowerWindow window;

    private final static int WINDOW_SIZE = 1200;    //en unité de temps de 0.1µs (=unité de temps de l'échantillonnage)

    private final static int IMPULSION_TIME = 5; //en unité de temps de 0.1µs (=unité de temps de l'échantillonnage)

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        window = new PowerWindow(samplesStream, WINDOW_SIZE);
    }

    /**
     * ATTENTION CHANGER LES INDEXES PLUS TARD POUR Y METRE DES CONSTANTES
     */
    public RawMessage nextMessage() throws IOException{
        RawMessage rawMessage;
        byte[] bytes;

        int sommePorteuseEmise0;
        int sommePorteuseEmise1;
        int sommePorteuseEmise2;
        int sommePorteuseNonEmise;

        //le nom de la variable ne correspond pas mais il va etre reafecté juste apres
        sommePorteuseEmise1 = calculSommePorteuse(0,2*IMPULSION_TIME,7*IMPULSION_TIME,9*IMPULSION_TIME);  //tout premier calcul de somme porteuse
        sommePorteuseEmise2 = calculSommePorteuse(1, 1 + 2*IMPULSION_TIME, 1 + 7*IMPULSION_TIME, 1 + 9*IMPULSION_TIME);  //2e calcul de somme porteuse

        while (window.isFull()){

            sommePorteuseEmise0 = sommePorteuseEmise1;  //pour optimiser le temps et eviter de recalculer les sommes porteuses déjà calculées avant
            sommePorteuseEmise1 = sommePorteuseEmise2;
            sommePorteuseEmise2 = calculSommePorteuse(2, 2 + 2*IMPULSION_TIME, 2 + 7*IMPULSION_TIME, 2 + 9*IMPULSION_TIME);
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

    private int calculSommePorteuse(int i1,int i2,int i3,int i4){
        return window.get(i1)+ window.get(i2)+window.get(i3)+window.get(i4);
    }

    private int calculSommeNonPorteuse(){
        return window.get(1 + IMPULSION_TIME)+ window.get(1 + 3*IMPULSION_TIME)+window.get(1 + 4*IMPULSION_TIME)+window.get(1 + 5*IMPULSION_TIME)+window.get(1 + 7*IMPULSION_TIME)+window.get(1 + 8*IMPULSION_TIME);
    }

    private byte calculBit(int i){
        return (byte) ((window.get(16*IMPULSION_TIME + 2*IMPULSION_TIME * i) < window.get(17*IMPULSION_TIME + 2*IMPULSION_TIME * i)) ? 0 : 1);
    }
}

