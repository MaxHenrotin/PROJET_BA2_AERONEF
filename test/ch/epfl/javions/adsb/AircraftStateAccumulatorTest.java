package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class AircraftStateAccumulatorTest {

    @Test
    void AircraftStateAccumulatorWorksOnSample() throws IOException {
        String f = "resources\\samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftState> a = new AircraftStateAccumulator<>(new AircraftState());
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;

                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
    }

    @Test
    void AircraftStateAccumulatorEdTest() throws IOException{

        String[] icaoAdressesString = {"4B17E5","495299","39D300","4241A9","4B1A00"};
        IcaoAddress[] icaoAdresses = new IcaoAddress[icaoAdressesString.length];
        for (int i = 0; i < icaoAdresses.length; i++) {
            icaoAdresses[i] = new IcaoAddress(icaoAdressesString[i]);
        }

        String f = "resources\\samples_20230304_1442.bin";
        IcaoAddress expectedAddress = icaoAdresses[4];
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftState> a = new AircraftStateAccumulator<>(new AircraftState());
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;

                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
/*
        1) IcaoAddress[string=4B17E5]

        position : (5.853790249675512°, 46.01809747517109°)
        position : (5.851936340332031°, 46.0194474644959°)
        position : (5.8511575777083635°, 46.02012631483376°)
        position : (5.850085783749819°, 46.021087635308504°)
        indicatif : CallSign[string=SWR6LH]

        2) IcaoAddress[string=495299]

        position : (6.025726301595569°, 46.35080630891025°)
        position : (6.024393243715167°, 46.350219743326306°)
        indicatif : CallSign[string=TAP931]
        position : (6.023388337343931°, 46.349807772785425°)
        position : (6.0221825167536736°, 46.34930418804288°)
        position : (6.021177694201469°, 46.34884645231068°)
        position : (6.0198211669921875°, 46.348292492330074°)
        position : (6.016354411840439°, 46.346786515787244°)
        position : (6.013847384601831°, 46.345732072368264°)

        3) IcaoAddress[string=39D300]

        position : (5.480049150064588°, 45.700099132955074°)
        position : (5.4783592745661736°, 45.701889004558325°)

        4) IcaoAddress[string=4241A9]

        position : (6.170471208170056°, 46.27934857271612°)
        position : (6.171100856736302°, 46.27981569617987°)

        5) IcaoAddress[string=4B1A00]

        position : (6.106187794357538°, 46.10270692035556°)
        position : (6.105048945173621°, 46.102020274847746°)
        position : (6.1043472122401°, 46.101658679544926°)
        position : (6.103797946125269°, 46.10133287496865°)
        position : (6.1029052734375°, 46.10082074068487°)
        position : (6.102235391736031°, 46.100418074056506°)
        indicatif : CallSign[string=SAZ54]
        position : (6.101632481440902°, 46.10009763389826°)
        position : (6.100225662812591°, 46.09931945800781°)
        position : (6.099622752517462°, 46.098999017849565°)
        position : (6.098216017708182°, 46.09822084195912°)
        */


    }

}