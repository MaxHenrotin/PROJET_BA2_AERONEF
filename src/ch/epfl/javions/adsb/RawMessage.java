package ch.epfl.javions.adsb;

import ch.epfl.javions.*;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

public record RawMessage(long timeStampNs, ByteString bytes) {

    public static final int LENGTH = 14;

    private static final int DFValue = 17;

    private static final int METypeLenght = 5;

    public RawMessage{
        Preconditions.checkArgument(timeStampNs>=0 && bytes.size()==LENGTH);
    }

    public static RawMessage of(long timeStampNs, byte[] bytes){
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        return (0 == crc24.crc(bytes)) ? new RawMessage(timeStampNs,new ByteString(bytes)) : null ;
    }

    public static int size(byte byte0){
        if(Bits.extractUInt(byte0,3,5) == 17){
            return LENGTH;
        }else {
            return 0;
        }
    }
    public static int typeCode(long payload){
        return  Bits.extractUInt(payload,Long.SIZE-METypeLenght,METypeLenght);
    }

    public int downLinkFormat(){
        return Bits.extractUInt(bytes.byteAt(0),3,5);
    }

    public IcaoAddress icaoAddress(){
        HexFormat hexFormat = HexFormat.of().withUpperCase();
        return new IcaoAddress( hexFormat.toHexDigits(bytes.bytesInRange(1,4),6));
    }

    public long payload(){
        return bytes.bytesInRange(4,11);
    }

    public int typeCode(){
        return Bits.extractUInt(bytes.byteAt(10),Byte.SIZE-METypeLenght,METypeLenght);
    }

}
