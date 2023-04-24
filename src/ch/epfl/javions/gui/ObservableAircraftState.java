package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AirbornePositionMessage;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    private ObjectProperty<CallSign> callSign;

    private ObjectProperty<GeoPos> position;

    private ObservableList<AirbornePosition> trajectory;

    private DoubleProperty altitude;

    private DoubleProperty velocity;

    private DoubleProperty trackOrHeading;




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

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
    }

    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    public ObservableList<AirbornePosition> trajectoryProperty() {
        return FXCollections.unmodifiableObservableList(trajectory);
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    public record AirbornePosition(GeoPos position,int altitude){}
}
