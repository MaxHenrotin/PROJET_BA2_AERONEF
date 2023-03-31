package ch.epfl.javions.adsb;
//  Author:    Max Henrotin

import ch.epfl.javions.GeoPos;

public class AircraftState implements AircraftStateSetter {

    @Override
    public void setCallSign(CallSign callSign) {
        System.out.println("indicatif : " + callSign);
    }

    @Override
    public void setPosition(GeoPos position) {
        System.out.println("position : " + position);
    }

    @Override
    public void setCategory(int category) {
        //System.out.println("catégorie : " + category);
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        //System.out.println("horodatage : " + timeStampNs);
    }

    @Override
    public void setAltitude(double altitude) {
        //System.out.println("altitude : " + altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        //System.out.println("vitesse : " + velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        //System.out.println("direction : " + trackOrHeading);
    }

}
