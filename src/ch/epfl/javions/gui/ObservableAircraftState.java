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

    private LongProperty lastMessageTimeStampNs;

    private IntegerProperty category;

    private ObjectProperty<CallSign> callSign;

    private ObjectProperty<GeoPos> position;

    private DoubleProperty altitude;

    private DoubleProperty velocity;

    private DoubleProperty trackOrHeading;

    private ObservableList<AirbornePosition> trajectory;

    private ObservableList<AirbornePosition> viewOfTrajectory;

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
        return lastMessageTimeStampNs.getValue();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    public int getCategory(){
        return category.getValue();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    public CallSign getCallSign() {
        return callSign.getValue();
    }

    @Override
    public void setPosition(GeoPos position) {
        updateTrajectory(new AirbornePosition(position,this.altitude.getValue()));

        this.position.set(position);
    }

    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    public GeoPos getPosition(){
        return position.getValue();
    }

    @Override
    public void setAltitude(double altitude) {
        updateTrajectory(new AirbornePosition(this.position.getValue(),altitude));

        this.altitude.set(altitude);
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public double getAltitude() {
        return altitude.getValue();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    public double getVelocity() {
        return velocity.getValue();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }
    public double getTrackOrHeading() {
        return trackOrHeading.getValue();
    }

    public ObservableList<AirbornePosition> trajectoryProperty() {
        return viewOfTrajectory;
    }

    private void updateTrajectory(AirbornePosition airbornePosition){
        if(trajectory.isEmpty() || !Objects.equals(this.position.getValue(),airbornePosition.position())){

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
