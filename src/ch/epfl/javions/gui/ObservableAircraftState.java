package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AirbornePositionMessage;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;

import javax.swing.text.Position;
import java.util.List;


/**
 * représente l'état d'un aeronef (observable au sens du patron de conception Observer)
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;


    private LongProperty lastMessageTimeStampNs;

    private IntegerProperty category;

    private ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>();

    private ObjectProperty<GeoPos> position = new SimpleObjectProperty<>();

    private DoubleProperty altitude;

    private DoubleProperty velocity;

    private DoubleProperty trackOrHeading;

    private List<AirbornePosition> trajectory;


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
        this.lastMessageTimeStampNs.set(timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public record AirbornePosition(Position position,int altitude){}
}
