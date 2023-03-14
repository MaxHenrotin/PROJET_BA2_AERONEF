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

    public RawMessage nextMessage() throws IOException{
        calculsSommes();

        byte[] bytes;

        while (window.isFull()){
            System.out.println("les sommes : "+sommePorteuseEmise0+"  "+sommePorteuseEmise1+"  "+sommePorteuseEmise2+"  "+sommePorteuseNonEmise);
            if ((sommePorteuseEmise0 < sommePorteuseEmise1) && (sommePorteuseEmise1 > sommePorteuseEmise2) && (sommePorteuseEmise1 >= 2*sommePorteuseNonEmise)){
                bytes = new byte[messageLength/Byte.SIZE];

                for (int i = 0; i < Byte.SIZE; i++) {
                    bytes[0] = (byte) ((bytes[0]<<1) | calculByte(i));
                }

                System.out.println("first byte : "+bytes[0]);

                if(RawMessage.size(bytes[0]) == RawMessage.LENGTH){
                    for (int i = Byte.SIZE; i < messageLength; i++) {
                        bytes[i/Byte.SIZE] = (byte) ((bytes[i/Byte.SIZE]<<1) | calculByte(i));
                    }
                    window.advanceBy(windowSize);

                    return new RawMessage(window.position()*100,new ByteString(bytes));

                    //rawMessage = RawMessage.of(window.position()+10,bytes);

                    /*if (rawMessage != null) {
                        window.advanceBy(windowSize);
                        return rawMessage;
                    }*/


                }
            }
            window.advance();
            calculsSommes();
        }
        return null;
    }

    private int calculSommePorteuseEmise(int index1, int index2, int index3,int index4){
        return window.get(index1) + window.get(index2) + window.get(index3)+window.get(index4);
    }

    private int calculSommePorteuseNonEmise(int index1, int index2, int index3,int index4,int index5,int index6){
        return window.get(index1) + window.get(index2) + window.get(index3) + window.get(index4) + window.get(index5) + window.get(index6);
    }

    private void calculsSommes(){
        sommePorteuseEmise0 = calculSommePorteuseEmise(0,10,35,45);
        sommePorteuseEmise1 = calculSommePorteuseEmise(10,20,45,55);
        sommePorteuseEmise2 = calculSommePorteuseEmise(20,30,55,65);

        sommePorteuseNonEmise = calculSommePorteuseNonEmise(5,15,20,25,30,40);
    }

    private byte calculByte(int i){
        return (byte) ((window.get(80 + 10 * i) < window.get(85 + 10*i)) ? 0 : 1);
    }
}