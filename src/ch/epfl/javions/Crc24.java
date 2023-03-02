package ch.epfl.javions;

public final class Crc24 {

    public final static int GENERATOR = 0xFFF409;

    private final static int CRC24LENGTH = 24;

    private final int generator;

    public Crc24(int generator){
        this.generator=generator;
    }

    public int crc(byte[] bytes){
        return crc_bitwise(generator,bytes);
    }

    private static int crc_bitwise(int generator,byte[] bytes){

        int crc24 = 0;

        int maskCrc24= 1 << CRC24LENGTH;

        int b;

        for (int elem : bytes){
            for (int j=0 ; j<Byte.SIZE ; ++j){
                b = Bits.extractUInt(elem,Byte.SIZE - (j+1),1);

                if(Bits.testBit(crc24,CRC24LENGTH-1)){

                    crc24 = ((crc24 << 1) | b) ^ (generator & (maskCrc24-1));

                }else {
                    crc24= (crc24 << 1) | b;
                }

                crc24 = crc24 & (maskCrc24-1);
            }
        }

        for (int k=0; k<CRC24LENGTH ;++k){
            if(Bits.testBit(crc24,CRC24LENGTH-1)){

                crc24 = (crc24 << 1) ^ (generator & (maskCrc24-1));

            }else {
                crc24= crc24 << 1;
            }

            crc24 = crc24 & (maskCrc24-1);

        }

        return crc24;
    }

}
