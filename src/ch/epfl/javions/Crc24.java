package ch.epfl.javions;

/**
 * Classe Crc24 permettant de calculer un crc24 à partir d'un generator donné
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

    private static int crc_bitwise(int generator,byte[] bytes){

        int crc24 = 0;

        int mask24= 1 << CRC24LENGTH;

        int mask23=1<<(CRC24LENGTH-1);

        int b;

        int newGenerator=(generator &  mask24-1);

        int[] table = {0,newGenerator};

        for (byte elem : bytes){
            for (int j=0 ; j<Byte.SIZE ; ++j){
                b = Bits.extractUInt(elem,Byte.SIZE - (j+1),1);

                crc24 = ((crc24 << 1) | b) ^ table[(crc24 & mask23)>>>(CRC24LENGTH-1)];

                crc24 = crc24 &  mask24-1;
            }
        }

        for (int k=0; k<CRC24LENGTH ;++k){

            crc24 = (crc24 << 1) ^ table[(crc24 & mask23)>>>(CRC24LENGTH-1)];

            crc24 = crc24 &  mask24-1;

        }

        return crc24;
    }


    private int[] buildTable(){
        int[] output= new int[256];

        for (int i=0;i< output.length;++i){
            byte[] byteInTab = {(byte)i};
            output[i]=crc_bitwise(generator,byteInTab);
        }

        return output;
    }


}
