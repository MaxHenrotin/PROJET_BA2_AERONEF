package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;


/**
 * représente l'état d'un aeronef (observable au sens du patron de conception Observer)
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class ObservableAircraftState implements AircraftStateSetter {

    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;


    /**
     * Constructeur
     * @param icaoAddress : adresse OACI de l'aéronef dont l'état est destiné à être représenté par l'instance à créer
     * @param aircraftData : caractéristiques fixes de cet aéronef (si elles existent) provenant de la base de données
     *                       mictronics
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData){
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;

    }

    public IcaoAddress getIcaoAddress() { return icaoAddress; }

    public AircraftData getAircraftData() { return aircraftData; }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {

    }

    @Override
    public void setCategory(int category) {

    }

    @Override
    public void setCallSign(CallSign callSign) {

    }

    @Override
    public void setPosition(GeoPos position) {

    }

    @Override
    public void setAltitude(double altitude) {

    }

    @Override
    public void setVelocity(double velocity) {

    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {

    }

}
