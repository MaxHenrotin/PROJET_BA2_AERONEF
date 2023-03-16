package ch.epfl.javions.adsb;

import ch.epfl.javions.*;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

public record RawMessage(long timeStampNs, ByteString bytes) {

    /**
     * attribut public, statique et final de la classe représentant la longueur EN OCTET des messages ADS-B.
     */
    public static final int LENGTH = 14;

    private static final int DF_VALUE = 17;

    private static final int DF_FORMAT_LENGHT = 5;
    private static final int DF_BIT_POSITION_INDEX = 3;

    private static final int ICAO_BYTE_POSITION_INDEX = 1;

    private static final int ICAO_HEX_SIZE = 6;

    private static final int ME_BYTE_POSITION_INDEX = 4;

    private static final int CRC_BYTE_POSITION_INDEX = 11;

    private static final int ME_LENGHT = 56;
    private static final int ME_TYPE_LENGHT = 5;

    public RawMessage{
        Preconditions.checkArgument(timeStampNs>=0 && bytes.size()==LENGTH);
    }

    public static RawMessage of(long timeStampNs, byte[] bytes){
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        return (0 == crc24.crc(bytes)) ? new RawMessage(timeStampNs,new ByteString(bytes)) : null ;
    }

    public static int size(byte byte0){
        if(Bits.extractUInt(byte0, DF_BIT_POSITION_INDEX, DF_FORMAT_LENGHT) == DF_VALUE){
            return LENGTH;
        }else {
            return 0;
        }
    }
    public static int typeCode(long payload){
        return  Bits.extractUInt(payload, ME_LENGHT - ME_TYPE_LENGHT , ME_TYPE_LENGHT);
    }

    public int downLinkFormat(){
        return Bits.extractUInt(bytes.byteAt(0), DF_BIT_POSITION_INDEX, DF_FORMAT_LENGHT);
    }

    public IcaoAddress icaoAddress(){
        HexFormat hexFormat = HexFormat.of().withUpperCase();
        return new IcaoAddress( hexFormat.toHexDigits(bytes.bytesInRange(ICAO_BYTE_POSITION_INDEX,ME_BYTE_POSITION_INDEX), ICAO_HEX_SIZE));
    }

    public long payload(){
        return bytes.bytesInRange(ME_BYTE_POSITION_INDEX,CRC_BYTE_POSITION_INDEX);
    }

    public int typeCode(){
        return typeCode(payload());
    }

}
