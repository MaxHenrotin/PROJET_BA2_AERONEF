package ch.epfl.javions;

public final class Crc24 {

    public final static int GENERATOR = 0xFFF409;

    private final static int CRCLENGTH = 24;

    private final int generator;

    public Crc24(int generator){
        this.generator=generator;
    }

    public int crc(byte[] bytes){
        return crc_bitwise(generator,bytes);
    }

    private static int crc_bitwise(int generator,byte[] bytes){

        int crc24 = 0;

        int maskCrc24= 1 << CRCLENGTH;

        ByteString byteString=new ByteString(bytes);

        for (int i=0; i< bytes.length ;++i){

            int b=Bits.extractUInt(bytes[i]);

            crc24 = (crc24 << 1) | byteString.byteAt(i);

            if(Bits.testBit(crc24,CRCLENGTH)){
                crc24 = crc24 ^ generator;
            }

            crc24 = crc24 & (maskCrc24-1);
        }

        return Bits.extractUInt(crc24,Long.SIZE-CRCLENGTH,CRCLENGTH);
    }

}
