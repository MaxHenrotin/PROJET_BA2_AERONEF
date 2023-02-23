package ch.epfl.javions;
//  Author:    Max Henrotin

import java.util.Objects;

public class Bits {
    private Bits(){}
    public static int extractUInt(long value, int start, int size){
        if(size<=0 || size>=Integer.SIZE){
            throw new IllegalArgumentException();       //voir 3.9
        }
        Objects.checkFromIndexSize(start,size,Long.SIZE);   //throw un IndexOutOfBundsException
        value = value << Long.SIZE - size- start; // supprime tous les bits supplémentaires à gauche
        value = value >>> Long.SIZE - size;       // supprime tous les bits supplémentaires à droite (mets des 0)
        return (int) value;
    }

    public static boolean testBit(long value, int index){
        Objects.checkIndex(index,Long.SIZE); //vérifie que index est bien entre 0 et 64
        long maskIndex = 1L << index;  //voir notes de cours "Types entiers" 5.5.1
        return (value & maskIndex) == maskIndex;    //voir notes de cours "Types entiers" 5.5.2
    }
}
