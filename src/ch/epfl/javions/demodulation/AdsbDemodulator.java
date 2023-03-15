package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {
    private PowerWindow window;

    private final static int messageLength = 112;

    private final static int windowSize = 1200;

    private RawMessage rawMessage;


    private int sommePorteuseEmise0, sommePorteuseEmise1, sommePorteuseEmise2, sommePorteuseNonEmise;

    private final static int INDEX0 = 0;

    private final static int INDEX10 = 10;

    private final static int INDEX35 = 35;

    private final static int INDEX45 = 45;

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        window = new PowerWindow(samplesStream,windowSize);
    }

    /**
     * ATTENTION CHANGER LES INDEXES PLUS TARD POUR Y METRE DES CONSTANTES
     */
    public RawMessage nextMessage() throws IOException{

        byte[] bytes =  new byte[messageLength/Byte.SIZE];  //tableau qui va stocker le message en octets

        //le nom de la variable ne correspond pas mais il va etre reafecté juste apres
        sommePorteuseEmise1 = calculSommePorteuse(INDEX0, INDEX10, INDEX35, INDEX45);  //tout premier calcul de somme porteuse
        sommePorteuseEmise2 = calculSommePorteuse(INDEX0+1, INDEX10+1, INDEX35+1, INDEX45+1);  //2e calcul de somme porteuse

        while (window.isFull()){

            sommePorteuseEmise0 = sommePorteuseEmise1;  //pour optimiser le temps et eviter de recalculer les sommes porteuses déjà calculées avant
            sommePorteuseEmise1 = sommePorteuseEmise2;
            sommePorteuseEmise2 = calculSommePorteuse(INDEX0+2, INDEX10+2, INDEX35+2, INDEX45+2);
            sommePorteuseNonEmise = calculSommeNonPorteuse();

            if ((sommePorteuseEmise0 < sommePorteuseEmise1) && (sommePorteuseEmise1 > sommePorteuseEmise2) && (sommePorteuseEmise1 >= 2*sommePorteuseNonEmise)){
                window.advance();

                for (int i = 0; i < Byte.SIZE; i++) {
                    bytes[0] = (byte) ((bytes[0]<<1) | calculBit(i));
                }

                if(RawMessage.size(bytes[0]) == RawMessage.LENGTH){

                    for (int i = Byte.SIZE; i < messageLength; i++) {
                        bytes[i/Byte.SIZE] = (byte) ((bytes[i/Byte.SIZE]<<1) | calculBit(i));
                    }

                    rawMessage = RawMessage.of(window.position()*100,bytes);
                    if (rawMessage != null) {
                        window.advanceBy(windowSize);
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

    private int  calculSommeNonPorteuse(){
        return window.get(6)+ window.get(16)+window.get(21)+window.get(26)+window.get(31)+window.get(41);
    }

    private byte calculBit(int i){
        return (byte) ((window.get(80 + 10 * i) < window.get(85 + 10*i)) ? 0 : 1);
    }
}

