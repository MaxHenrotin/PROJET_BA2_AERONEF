package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

public final class ByteString {

    private byte[] tab;

    public ByteString(byte[] bytes){
        tab=bytes.clone();
    }

    public static ByteString ofHexadecimalString(String hexString){
        if((hexString.length()%2)!=0){
            throw new IllegalArgumentException();
        }
        ByteString output;

        try{
            output=new ByteString(HexFormat.of().parseHex(hexString));
        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException();
        }

        return output;
    }

    public int size(){return tab.length;}

    public int byteAt(int index){
        if(index<0||index>= tab.length){
            throw new IndexOutOfBoundsException();
        }else{
            return tab[index];
        }
    }

    public long bytesInRange(int fromIndex, int toIndex){
        Objects.checkFromToIndex(fromIndex,toIndex,tab.length);

        if(toIndex-fromIndex>=Long.SIZE){
            throw new IllegalArgumentException();
        }

        byte[] output=java.util.Arrays.copyOfRange(tab,fromIndex,toIndex);

        long number=output[0];

        for(int i = 1; i<output.length; ++i){
            number = number << 8;
            int unsigned = output[i] & 0b11_11_11_11;  //evite des potentiels problemes de signes
            number = number | unsigned;
        }
        return number;

    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof ByteString that){
            return Arrays.equals(tab,that.tab);

        }else{
            throw new IllegalArgumentException();
        }

    }

    public String toString(Object object){
        HexFormat hexFormat=HexFormat.of().withUpperCase();
        if(object instanceof ByteString that){
            return hexFormat.formatHex(that.tab).toUpperCase();
        }else{
            throw new NumberFormatException();
        }

    }

    public static int hashcode(Object object){
        if(object instanceof ByteString that){
            return that.tab.hashCode();
        }else{
            throw new IllegalArgumentException();
        }

    }
}
