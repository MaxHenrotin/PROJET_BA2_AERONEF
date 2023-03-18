package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress,double altitude, int parity, double x, double y) implements Message{

    public AirbornePositionMessage{
        //pour commit
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs>=0 && (parity==0 || parity==0) && (x>=0 && x<1) && (y>=0 && y<1));
    }

    public static AirbornePositionMessage of(RawMessage rawMessage){
        return null;
    }
}
