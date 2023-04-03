package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
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
    void aircraftStateAccumulatorConstructorThrowsIfStateSetterIsNull() {
        assertThrows(NullPointerException.class, () -> new AircraftStateAccumulator<>(null));
    }

    @Test
    void aircraftStateSetterStateSetterReturnsStateSetter() {
        for (int i = 0; i < 10; i += 1) {
            var stateSetter = new AircraftState();
            var accumulator = new AircraftStateAccumulator<>(stateSetter);
            assertSame(stateSetter, accumulator.stateSetter());
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesCategoryAndCallSign() {
        var icao = new IcaoAddress("ABCDEF");
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);
        var expectedLastMessageTimeStampNs = -1L;
        var expectedCategory = -1;
        var expectedCallSign = (CallSign) null;
        for (var i = 0; i < 8; i += 1) {
            assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
            assertEquals(expectedCategory, stateSetter.category);
            assertEquals(expectedCallSign, stateSetter.callSign);

            expectedLastMessageTimeStampNs = 101L * i;
            expectedCategory = 0xA0 | i;
            expectedCallSign = new CallSign("BLA" + Integer.toString(i, 3));
            var message = new AircraftIdentificationMessage(expectedLastMessageTimeStampNs, icao, expectedCategory, expectedCallSign);
            accumulator.update(message);
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesVelocityAndTrackOrHeading() {
        var icao = new IcaoAddress("ABCDEF");
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);
        var expectedLastMessageTimeStampNs = -1L;
        var expectedVelocity = Double.NaN;
        var expectedTrackOrHeading = Double.NaN;
        for (var i = 0; i < 8; i += 1) {
            assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
            assertEquals(expectedVelocity, stateSetter.velocity);
            assertEquals(expectedTrackOrHeading, stateSetter.trackOrHeading);

            expectedLastMessageTimeStampNs = 103L * i;
            expectedVelocity = 10.0 * i;
            expectedTrackOrHeading = 1.99999999 * Math.PI / (i + 1);
            var message = new AirborneVelocityMessage(expectedLastMessageTimeStampNs, icao, expectedVelocity, expectedTrackOrHeading);
            accumulator.update(message);
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesAltitudeButNotPositionWhenParityIsConstant() {
        var icao = new IcaoAddress("ABCDEF");
        for (int parity = 0; parity <= 1; parity += 1) {
            var stateSetter = new AircraftState();
            var accumulator = new AircraftStateAccumulator<>(stateSetter);

            var expectedLastMessageTimeStampNs = -1L;
            var expectedAltitude = Double.NaN;
            for (int i = 0; i < 100; i += 1) {
                assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
                assertEquals(expectedAltitude, stateSetter.altitude);
                assertNull(stateSetter.position);

                expectedLastMessageTimeStampNs = 107L * i;
                expectedAltitude = -100d + 20d * i;
                var x = 0.999999 / (i + 1d);
                var y = 1d - x;
                var message = new AirbornePositionMessage(expectedLastMessageTimeStampNs, icao, expectedAltitude, parity, x, y);
                accumulator.update(message);
            }
        }
    }

    @Test
    void aircraftStateAccumulatorUpdateUpdatesAltitudeButNotPositionWhenMessagesTooFarApart() {
        var icao = new IcaoAddress("ABCDEF");
        var moreThan10s = 10_000_000_001L;
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);

        var x = 0.5;
        var y = 0.5;
        var parity = 0;
        var expectedLastMessageTimeStampNs = -1L;
        var expectedAltitude = Double.NaN;
        for (int i = 0; i < 100; i += 1) {
            assertEquals(expectedLastMessageTimeStampNs, stateSetter.lastMessageTimeStampNs);
            assertEquals(expectedAltitude, stateSetter.altitude);
            assertNull(stateSetter.position);

            expectedLastMessageTimeStampNs += moreThan10s;
            expectedAltitude = -100d + 23d * i;
            parity = 1 - parity;
            var message = new AirbornePositionMessage(expectedLastMessageTimeStampNs, icao, expectedAltitude, parity, x, y);
            accumulator.update(message);
        }
    }

    double cpr(int v) {
        return Math.scalb((double) v, -17);
    }

    @Test
    void aircraftStateAccumulatorUpdateUsesCorrectMessageToComputePosition() {
        var icao = new IcaoAddress("ABCDEF");
        var moreThan10s = 10_000_000_001L;
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);

        var timeStampNs = 109L;
        var altitude = 1000d;
        var x0 = cpr(98152);
        var y0 = cpr(98838);
        var x1 = cpr(95758);
        var y1 = cpr(81899);

        var m1 = new AirbornePositionMessage(timeStampNs, icao, altitude, 0, cpr(12), cpr(13));
        accumulator.update(m1);
        assertNull(stateSetter.position);

        timeStampNs += moreThan10s;
        var m2 = new AirbornePositionMessage(timeStampNs, icao, altitude, 0, x0, y0);
        accumulator.update(m2);
        assertNull(stateSetter.position);

        timeStampNs += 1000L;
        var m3 = new AirbornePositionMessage(timeStampNs, icao, altitude, 1, x1, y1);
        accumulator.update(m3);
        assertNotNull(stateSetter.position);
        var p = stateSetter.position;
        assertEquals(6.57520, Math.toDegrees(p.longitude()), 5e-5);
        assertEquals(46.52444, Math.toDegrees(p.latitude()), 5e-5);
    }

    @Test
    void aircraftStateAccumulatorCorrectlyHandlesLatitudeBandChange() {
        record ParityXY(int p, int x, int y) { }

        var xys = new ParityXY[]{
                new ParityXY(0, 98152, 106326),
                new ParityXY(1, 95758, 89262),
                new ParityXY(0, 95758, 106330),
                new ParityXY(1, 93364, 89266)
        };
        var expectedLongitudeDeg = 6.57520;
        var expectedLatitudesDeg = new double[]{
                Double.NaN, 46.8672, 46.8672, 46.8674
        };

        var icao = new IcaoAddress("ABCDEF");
        var stateSetter = new AircraftState();
        var accumulator = new AircraftStateAccumulator<>(stateSetter);

        var timeStampNs = 113L;
        var altitude = 567d;

        for (int i = 0; i < xys.length; i += 1) {

            var m = new AirbornePositionMessage(timeStampNs, icao, altitude, xys[i].p, cpr(xys[i].x), cpr(xys[i].y));
            accumulator.update(m);
            var expectedLatitudeDeg = expectedLatitudesDeg[i];
            if (Double.isNaN(expectedLatitudeDeg)) {
                assertNull(stateSetter.position);
            } else {
                assertEquals(expectedLongitudeDeg, Math.toDegrees(stateSetter.position.longitude()), 1e-4);
                assertEquals(expectedLatitudeDeg, Math.toDegrees(stateSetter.position.latitude()), 1e-4);
            }
            timeStampNs += 1000L;
        }
    }

    /*
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

