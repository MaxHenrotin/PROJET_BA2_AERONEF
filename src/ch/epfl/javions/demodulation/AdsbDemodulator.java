package ch.epfl.javions.demodulation;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.demodulation.PowerWindow;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {

    private final InputStream samplesStream;

    private PowerWindow window;

    private final static int messageLength = 112;

    private final static int windowSize = 1200;

    private RawMessage rawMessage;


    private int sommePorteuseEmise0;

    private int sommePorteuseEmise1;

    private int sommePorteuseEmise2;

    private int sommePorteuseNonEmise;

    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.samplesStream=samplesStream;
        window = new PowerWindow(samplesStream,windowSize);
    }

    /**
     * ATTENTION CHANGER LES INDEXES PLUS TARD POUR Y METRE DES CONSTANTES
     */
    public RawMessage nextMessage() throws IOException{
        byte[] bytes;

        //le nom de la variable ne correspond pas mais il va etre reafecté juste apres
        sommePorteuseEmise1 = calculSommePorteuse(0,10,35,45);  //tout premier calcul de somme porteuse
        sommePorteuseEmise2 = calculSommePorteuse(1,11,36,46);  //2e calcul de somme porteuse

        while (window.isFull()){

            sommePorteuseEmise0 = sommePorteuseEmise1;  //pour optimiser le temps et eviter de recalculer les sommes porteuses déjà calculées avant
            sommePorteuseEmise1 = sommePorteuseEmise2;
            sommePorteuseEmise2 = calculSommePorteuse(2,12,37,47);
            sommePorteuseNonEmise = calculSommeNonPorteuse(6,16,21,26,31,41);

            if ((sommePorteuseEmise0 < sommePorteuseEmise1) && (sommePorteuseEmise1 > sommePorteuseEmise2) && (sommePorteuseEmise1 >= 2*sommePorteuseNonEmise)){
                window.advance();

                bytes = new byte[messageLength/Byte.SIZE];

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

    private int calculSommeNonPorteuse(int i1,int i2,int i3,int i4,int i5,int i6){
        return window.get(i1)+ window.get(i2)+window.get(i3)+window.get(i4)+window.get(i5)+window.get(i6);
    }

    private byte calculBit(int i){
        return (byte) ((window.get(80 + 10 * i) < window.get(85 + 10*i)) ? 0 : 1);
    }
}

