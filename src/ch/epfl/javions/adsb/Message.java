package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

public interface Message {

    public long timeStampNs();

    IcaoAddress icaoAddress();
}
