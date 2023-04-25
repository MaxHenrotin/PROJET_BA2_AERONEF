package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;


/**
 * représente l'état d'un aeronef (observable au sens du patron de conception Observer)
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class ObservableAircraftState implements AircraftStateSetter {
    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;

    private final LongProperty lastMessageTimeStampNs;

    private final IntegerProperty category;

    private final ObjectProperty<CallSign> callSign;

    private final ObjectProperty<GeoPos> position;

    private final DoubleProperty altitude;

    private final DoubleProperty velocity;

    private final DoubleProperty trackOrHeading;

    private final ObservableList<AirbornePosition> trajectory;

    private final ObservableList<AirbornePosition> viewOfTrajectory;

    private long lastUpdateTrajectoryTimeStampsNs;


    /**
     * Constructeur
     * @param icaoAddress : adresse OACI de l'aéronef dont l'état est destiné à être représenté par l'instance à créer
     * @param aircraftData : caractéristiques fixes de cet aéronef (si elles existent) provenant de la base de données
     *                       mictronics
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData){
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;

        lastMessageTimeStampNs = new SimpleLongProperty();
        category = new SimpleIntegerProperty();
        callSign = new SimpleObjectProperty<>();
        position = new SimpleObjectProperty<>();
        altitude = new SimpleDoubleProperty();
        velocity = new SimpleDoubleProperty();
        trackOrHeading = new SimpleDoubleProperty();

        trajectory = FXCollections.observableArrayList();
        viewOfTrajectory = FXCollections.unmodifiableObservableList(trajectory);
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

    public long getLastMessageTimeStampsNs(){
        return lastMessageTimeStampNs.get();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    public int getCategory(){
        return category.get();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    public CallSign getCallSign() {
        return callSign.get();
    }

    @Override
    public void setPosition(GeoPos position) {
        updateTrajectory(new AirbornePosition(position,this.altitude.get()));

        this.position.set(position);
    }

    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    public GeoPos getPosition(){
        return position.get();
    }

    @Override
    public void setAltitude(double altitude) {
        updateTrajectory(new AirbornePosition(this.position.get(),altitude));

        this.altitude.set(altitude);
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public double getAltitude() {
        return altitude.get();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    public double getVelocity() {
        return velocity.get();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    public ObservableList<AirbornePosition> trajectoryProperty() {
        return viewOfTrajectory;
    }

    private void updateTrajectory(AirbornePosition airbornePosition){
        if(trajectory.isEmpty() || !Objects.equals(this.position.get(),airbornePosition.position())){

            lastUpdateTrajectoryTimeStampsNs = lastMessageTimeStampNs.getValue();
            if(airbornePosition.position() != null){
                trajectory.add(airbornePosition);
            }

        }else if(lastUpdateTrajectoryTimeStampsNs == lastMessageTimeStampNs.getValue()){
            trajectory.remove(trajectory.size() - 1);
            trajectory.add(airbornePosition);
        }
    }

    public record AirbornePosition(GeoPos position, double altitude){}
}
