package ch.epfl.javions;

public final class Crc24 {

    public final static int GENERATOR = 0xFFF409;

    private final static int crcLength=24;

    private final int generator;

    public Crc24(int generator){
        this.generator=generator;
    }

    public int crc(byte[] bytes){
        return crc_bitwise(generator,bytes);
    }

    private static int crc_bitwise(int generator,byte[] bytes){
        int crc24=0;
        byte[] messAugmente=new byte[bytes.length + crcLength];

        int maskCrc24= 1 << crcLength;
        //int maskTest= (1<<crcLength)-1;


        for (int i=0;i< bytes.length;++i){
            messAugmente[i]=bytes[bytes.length-1-i];
        }

        ByteString byteString=new ByteString(messAugmente);

        for (int i=0;i<byteString.size();++i){

            crc24 = (crc24 << 1) | byteString.byteAt(i);

            if((crc24 & maskCrc24) == maskCrc24){
                crc24 = crc24 ^ generator;
            }

            crc24 = crc24 & (maskCrc24-1);
        }

        return crc24;
    }

}
