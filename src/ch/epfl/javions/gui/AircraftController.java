package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;


public final class AircraftController {

    private MapParameters mapParameters;
    private ObservableSet<ObservableAircraftState> states;
    private SimpleObjectProperty<ObservableAircraftState> currentAircraft;

    private ColorRamp plasma;

    private Pane pane;
    private Canvas canvas;

    public AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> states, SimpleObjectProperty<ObservableAircraftState> currentAicraftState){
        this.mapParameters = mapParameters;
        this.states = states;
        this.currentAircraft = currentAicraftState;
        plasma = ColorRamp.PLASMA;

        pane = new Pane();
        pane.setPickOnBounds(false);
    }

    private Node groupForAircraft(ObservableAircraftState aircraftState){
        Group trajectoryGroup = new Group();
        Group afficheAircraft = new Group();
        return new Group(trajectoryGroup,afficheAircraft);
    }

    private Node groupForAffichageAircraft(ObservableAircraftState aircraftState){
        Group etiquette = nodeForEtiquette(aircraftState);
        Group icone = new Group();

        return new Group(etiquette,icone);
    }

    private Node nodeForIcone(ObservableAircraftState aircraftState){
        AircraftData aircraftData = aircraftState.getAircraftData();
        AircraftIcon aircraftIcon = AircraftIcon.iconFor(aircraftData.typeDesignator(),aircraftData.description(),
                                                aircraftState.getCategory(),aircraftData.wakeTurbulenceCategory());

        SVGPath iconSVGPath = new SVGPath();

        if(aircraftIcon.canRotate()) {
            double angle = Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE);
        }

        if(aircraftState.altitudeProperty() == null){
            double colorRampValue = Math.pow((aircraftState.getAltitude()/12000),1/3);
            //plasma.colorAt(colorRampValue);
        }

        return null;
    }

    private Group nodeForEtiquette(ObservableAircraftState aircraftState) {
        //création du rectangle dans lequel mettre les infos
        Rectangle rectangle = new Rectangle(20, 20, Color.LIGHTGRAY);

        //création du texte d'info
        Text info;

        if (aircraftState.getAircraftData().registration() != null) {
            info = new Text(aircraftState.getAircraftData().registration().string());
        } else if (aircraftState.getCallSign() != null) {
            info = new Text(aircraftState.getCallSign().string());
        } else {
            info = new Text(aircraftState.getIcaoAddress().string());
        }

        //création du texte de vitesse et d'altitude
        Text speedAndAlt;

        String altitude;
        String speed;

        if (aircraftState.velocityProperty() == null) {
            altitude = "?";
        } else {
            altitude = String.valueOf(Units.convert(aircraftState.getAltitude(), Units.Length.FOOT, Units.Length.METER));
        }

        if (aircraftState.altitudeProperty() == null) {
            speed = "?";
        } else {
            speed = String.valueOf(Units.convert(aircraftState.getVelocity(), Units.Speed.KNOT, Units.Speed.KILOMETER_PER_HOUR));
        }

        StringBuilder speedAndAltString = new StringBuilder().
                append(speed).
                append("km/h").
                append("\u2002").
                append(altitude).
                append("m");

        speedAndAlt = new Text(speedAndAltString.toString());

        //retourne un groupe composé de tout ce qui est nécessaire pour l'étiquette
        return new Group(rectangle, info, speedAndAlt);
    }
    public Pane pane(){return pane;}
}
