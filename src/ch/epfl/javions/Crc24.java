package ch.epfl.javions;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Représente un calculateur de CRC de 24 bits
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class Crc24 {

    /**
     * Generator correspondant aux messages ADS-B
     */
    public final static int GENERATOR = 0xFFF409;


    /**
     * Constante représentant la longueur du CRC
     */
    private final static int CRC24LENGTH = 24;

    private final int generator;

    /**
     * Constructeur de la classe qui prend le générateur en argument pour décoder le message
     * @param generator : gémérateur utilisé pour calculer le crc24
     */
    public Crc24(int generator){
        this.generator=generator;
    }

    /**
     * Retourne le CRC24 du tableau donné avec un algorithme octet par octet
     * @param bytes : tableau de bytes représentant le message
     * @return le crc correspondant au message donné
     */
    public int crc(byte[] bytes){
        int[] table= buildTable();

        int crc = 0;

        int index;

        for (int i=0; i< bytes.length;++i){

            index = Bits.extractUInt(crc,CRC24LENGTH-1,Byte.SIZE);

            crc=((crc << 8) | bytes[i]) ^ table[index];

        }

        for (int k=0; k< CRC24LENGTH;++k){

            index = Bits.extractUInt(crc,CRC24LENGTH-1,Byte.SIZE);

            crc=(crc << 8) ^ table[index];

        }

        return crc_bitwise(generator,bytes);
    }

    /**
     * Retourne le CRC24 du tableau donné avec un algorithme bit à bit
     * @param generator : générateur utilisé pour calculer le crc
     * @param bytes : message donné
     * @return le crc corespondant au message donné calculé à l'aide du generator
     */
    private static int crc_bitwise(int generator,byte[] bytes){

        int crc24 = 0;

        int mask24= 1 << CRC24LENGTH;

        int mask23=1<<(CRC24LENGTH-1);

        int b;

        int index;

        int newGenerator=(generator &  mask24-1);

        int[] table = {0,newGenerator};

        for (byte elem : bytes){
            for (int j=0 ; j<Byte.SIZE ; ++j){
                b = Bits.extractUInt(elem,Byte.SIZE - (j+1),1);

                index=(crc24 & mask23)>>>(CRC24LENGTH-1);

                crc24 = ((crc24 << 1) | b) ^ table[index];

                crc24 = crc24 &  mask24-1;
            }
        }

        for (int k=0; k<CRC24LENGTH ;++k){

            index=(crc24 & mask23)>>>(CRC24LENGTH-1);

            crc24 = (crc24 << 1) ^ table[index];

            crc24 = crc24 &  mask24-1;

        }

        return crc24;
    }


    //Construit la table de tous les CRC24 possible
    private int[] buildTable(){
        int[] output= new int[256];

        for (int i=0;i< output.length;++i){
            byte[] byteInTab = {(byte)i};
            output[i]=crc_bitwise(generator,byteInTab);
        }

        return output;
    }


}
