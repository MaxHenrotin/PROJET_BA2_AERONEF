package ch.epfl.javions;

import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class Crc24Test {

    @Test
    void crcWorksOnNormalInput(){

        //******************************

        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        String mS = "8D392AE499107FB5C00439";
        String cS = "035DB8";
        int c = Integer.parseInt(cS, 16); // == 0x035DB8

        byte[] mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));

        byte[] mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));

        //******************************

        crc24 = new Crc24(Crc24.GENERATOR);
        mS = "8D4D2286EA428867291C08";
        cS = "EE2EC6";
        c = Integer.parseInt(cS, 16); // == 0x035DB8

        mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));

        mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));

        //******************************

        crc24 = new Crc24(Crc24.GENERATOR);
        mS = "8D3950C69914B232880436";
        cS = "BC63D3";
        c = Integer.parseInt(cS, 16); // == 0x035DB8

        mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));

        mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));

        //******************************

        crc24 = new Crc24(Crc24.GENERATOR);
        mS = "8D4B17E399893E15C09C21";
        cS = "9FC014";
        c = Integer.parseInt(cS, 16); // == 0x035DB8

        mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));

        mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));

        //******************************

        crc24 = new Crc24(Crc24.GENERATOR);
        mS = "8D4B18F4231445F2DB63A0";
        cS = "DEEB82";
        c = Integer.parseInt(cS, 16); // == 0x035DB8

        mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));

        mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));

        //******************************

        crc24 = new Crc24(Crc24.GENERATOR);
        mS = "8D495293F82300020049B8";
        cS = "111203";
        c = Integer.parseInt(cS, 16); // == 0x035DB8

        mAndC = HexFormat.of().parseHex(mS + cS);
        assertEquals(0, crc24.crc(mAndC));

        mOnly = HexFormat.of().parseHex(mS);
        assertEquals(c, crc24.crc(mOnly));


    }

}