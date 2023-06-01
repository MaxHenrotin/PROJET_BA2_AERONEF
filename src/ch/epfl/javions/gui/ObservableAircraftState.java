package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;



/**
 * représente l'état d'un aeronef (observable au sens du patron de conception Observer)
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class ObservableAircraftState implements AircraftStateSetter {

    //===================================== Attributs privées ==========================================================

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

    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur d'un état d'aéronef
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
        altitude = new SimpleDoubleProperty(Double.NaN);
        velocity = new SimpleDoubleProperty(Double.NaN);
        trackOrHeading = new SimpleDoubleProperty(Double.NaN);

        trajectory = FXCollections.observableArrayList();
        viewOfTrajectory = FXCollections.unmodifiableObservableList(trajectory);
    }

    /**
     * getter de l'adress OACI de l'aéronef
     * @return l'adresse OACI de l'aéronef
     */
    public IcaoAddress getIcaoAddress() { return icaoAddress; }

    /**
     * getter des caractéristiques fixes de l'aéronef (issues de la base de donnée mictronics et liées à l'adresse OACI)
     * @return les caractéristiques fixes de l'aéronef
     */
    public AircraftData getAircraftData() { return aircraftData; }

    /**
     * setter de l'horodatage de l'état de l'aéronef
     * @param timeStampNs : valeur du nouvel l'horodatage
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        this.lastMessageTimeStampNs.set(timeStampNs);
    }

    /**
     * lecteur de la propriété représentant l'horodatage de l'état de l'aéronef
     * @return la propriété représentant l'horodatage de l'état de l'aéronef en lecture seule
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    /**
     * getter de l'horodatage de l'état de l'aéronef
     * @return l'horodatage de l'état de l'aéronef
     */
    public long getLastMessageTimeStampsNs(){
        return lastMessageTimeStampNs.get();
    }

    /**
     * setter de la catégorie de l'aéronef
     * @param category : valeur de la nouvelle catégorie
     */
    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    /**
     * lecteur de la propriété représentant la catégorie de l'aéronef
     * @return la propriété représentant la catégorie de l'aéronef en lecture seule
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    /**
     * getter de la catégorie de l'aéronef
     * @return la catégorie de l'aéronef
     */
    public int getCategory(){
        return category.get();
    }

    /**
     * setter de l'indicatif de l'aéronef
     * @param callSign : valeur du nouvel indicatif
     */
    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    /**
     * lecteur de la propriété représentant l'indicatif de l'aéronef
     * @return la propriété représentant l'indicatif de l'aéronef en lecture seule
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    /**
     * getter de l'indicatif de l'aéronef
     * @return l'indicatif de l'aéronef
     */
    public CallSign getCallSign() {
        return callSign.get();
    }

    /**
     * setter de la position de l'aéronef
     * @param position : valeur de la nouvelle position
     */
    @Override
    public void setPosition(GeoPos position) {
        //met à jour la trajectoire
        trajectory.add(new AirbornePosition(position,this.altitude.get()));
        lastUpdateTrajectoryTimeStampsNs = lastMessageTimeStampNs.getValue();

        this.position.set(position);
    }

    /**
     * lecteur de la propriété représentant la position de l'aéronef
     * @return la propriété représentant la position de l'aéronef en lecture seule
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    /**
     * getter de la position de l'aéronef
     * @return la position de l'aéronef
     */
    public GeoPos getPosition(){
        return position.get();
    }

    /**
     * getter de la trajectoire de l'aéronef
     * @param altitude : valeur de la nouvelle altitude
     */
    @Override
    public void setAltitude(double altitude) {
        AirbornePosition airbornePosition = new AirbornePosition(this.position.get(), altitude);
        if(trajectory == null) { //si la trajectoire est vide, on ajoute la position actuelle
            trajectory.add(airbornePosition);
            lastUpdateTrajectoryTimeStampsNs = lastMessageTimeStampNs.getValue();
        }

        //si les deux derniers points de la trajectoire ont le même horodatage,
        //on remplace le dernier point par la nouvelle position
        if(lastUpdateTrajectoryTimeStampsNs == lastMessageTimeStampNs.getValue()){
            trajectory.remove(trajectory.size() - 1);
            trajectory.add(airbornePosition);
        }

        this.altitude.set(altitude);
    }

    /**
     * lecteur de la propriété représentant l'altitude de l'aéronef
     * @return la propriété représentant l'altitude de l'aéronef en lecture seule
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    /**
     * getter de l'altitude de l'aéronef
     * @return l'altitude de l'aéronef
     */
    public double getAltitude() {
        return altitude.get();
    }

    /**
     * setter de la vitesse de l'aéronef
     * @param velocity : valeur de la nouvelle vitesse
     */
    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    /**
     * lecteur de la propriété représentant la vitesse de l'aéronef
     * @return la propriété représentant la vitesse de l'aéronef en lecture seule
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    /**
     * getter de la vitesse de l'aéronef
     * @return la vitesse de l'aéronef
     */
    public double getVelocity() {
        return velocity.get();
    }

    /**
     * setter de la direction de l'aéronef
     * @param trackOrHeading : valeur de la nouvelle direction
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    /**
     * lecteur de la propriété représentant la direction de l'aéronef
     * @return la propriété représentant la direction de l'aéronef en lecture seule
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    /**
     * getter de la direction de l'aéronef
     * @return la direction de l'aéronef
     */
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    /**
     * lecteur de la collection observable représentant la trajectoire de l'aéronef (liste de positions)
     * @return la propriété représentant la trajectoire de l'aéronef (en lecture seule)
     */
    public ObservableList<AirbornePosition> trajectoryProperty() {
        return viewOfTrajectory;
    }

    /**
     * Enregistrement imbriqué représentant une position d'aéronef (dans les 3 dimensions)
     * @param position : position de l'aéronef (dans les 2 dimensions longitude et latitude)
     * @param altitude : altitude de l'aéronef
     */
    public record AirbornePosition(GeoPos position, double altitude){}

}
