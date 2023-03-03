package ch.epfl.javions;


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

    private final static int mask24= 1 << CRC24LENGTH;

    private final static int mask23bits=mask24-1;
    private final int[] table;

    /**
     * Constructeur de la classe qui prend le générateur en argument pour décoder le message et construit la table des crc24 correspondant
     * @param generator : gémérateur utilisé pour calculer le crc24
     */
    public Crc24(int generator){
        this.generator=generator;
        table= buildTable();
    }

    /**
     * Retourne le CRC24 du tableau donné avec un algorithme octet par octet
     * @param bytes : tableau de bytes représentant le message
     * @return le crc correspondant au message donné
     */
    public int crc(byte[] bytes){

        int crc = 0;

        ByteString byteString=new ByteString(bytes);

        int index;

        for (int i=0 ; i < byteString.size() ; ++i) {

            index = Bits.extractUInt(crc, CRC24LENGTH - Byte.SIZE, Byte.SIZE);

            crc = ((crc << Byte.SIZE) | byteString.byteAt(i)) ^ table[index];

        }

        for (int k=0; k< (CRC24LENGTH/Byte.SIZE);++k){

            index = Bits.extractUInt(crc,CRC24LENGTH-Byte.SIZE,Byte.SIZE);

            crc=(crc << Byte.SIZE) ^ table[index];

        }

        return crc & mask23bits;
    }

    /**
     * Retourne le CRC24 du tableau donné avec un algorithme bit à bit
     * @param generator : générateur utilisé pour calculer le crc
     * @param bytes : message donné
     * @return le crc corespondant au tableau de bytes donné calculé à l'aide du generator
     */
    private static int crc_bitwise(int generator,byte[] bytes){

        int crc24 = 0;

        int b;

        int index;

        int newGenerator=(generator &  mask23bits);

        int[] table = {0,newGenerator};

        for (byte elem : bytes){
            for (int j=0 ; j<Byte.SIZE ; ++j){
                b = Bits.extractUInt(elem,Byte.SIZE - j - 1,1);

                index=(crc24 & mask23bits)>>>(CRC24LENGTH-1);

                crc24 = ((crc24 << 1) | b) ^ table[index];

                crc24 = crc24 &  mask23bits;
            }
        }

        for (int k=0; k<CRC24LENGTH ;++k){

            index=(crc24 & mask23bits)>>>(CRC24LENGTH-1);

            crc24 = (crc24 << 1) ^ table[index];

            crc24 = crc24 &  mask23bits;

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
