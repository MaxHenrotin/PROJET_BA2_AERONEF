package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import com.sun.javafx.sg.prism.NGGroup;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;


public final class AircraftController {

    private MapParameters mapParameters;
    private ObservableSet<ObservableAircraftState> states;
    private ObjectProperty<ObservableAircraftState> currentAircraft;
    private ColorRamp plasma;
    private Pane pane;

    public AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> states, ObjectProperty<ObservableAircraftState> currentAicraftState){
        this.mapParameters = mapParameters;
        this.states = states;
        this.currentAircraft = currentAicraftState;
        plasma = ColorRamp.PLASMA;

        pane = new Pane();
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");

        layoutVisibleAircrafts();
    }

    private void layoutVisibleAircrafts(){
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if(change.wasAdded()){
                pane.getChildren().add(groupForAircraft(change.getElementAdded()));
            }else{
                String icaoAdressToRemove = change.getElementRemoved().getIcaoAddress().string();
                pane.getChildren().removeIf(node -> node.getId().equals(icaoAdressToRemove));
            }
        });
    }

    private Group groupForAircraft(ObservableAircraftState aircraftState){
        Group trajectoryGroup = groupForTrajectory(aircraftState);
        Group afficheAircraft = groupForAffichageAircraft(aircraftState);

        Group annotatedAircraft = new Group(trajectoryGroup,afficheAircraft);
        annotatedAircraft.setId(aircraftState.getIcaoAddress().string());

        return annotatedAircraft;
    }

    private Group groupForTrajectory(ObservableAircraftState aircraftState){

        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");
        trajectoryGroup.visibleProperty().bind(Bindings.createBooleanBinding(() -> Objects.nonNull(currentAircraft.get()) && aircraftState.equals(currentAircraft.get()),currentAircraft));

        trajectoryGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectoryGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());

        aircraftState.trajectoryProperty().addListener((ListChangeListener<ObservableAircraftState.AirbornePosition>) c -> {
            if (trajectoryGroup.isVisible()) drawTrajectory(trajectoryGroup, aircraftState.trajectoryProperty());
        });

        mapParameters.zoomProperty().addListener((observable, oldValue, newValue) -> {
            if(trajectoryGroup.isVisible())drawTrajectory(trajectoryGroup, aircraftState.trajectoryProperty());
        });

        trajectoryGroup.visibleProperty().addListener((observable, oldValue, newValue) ->
            drawTrajectory(trajectoryGroup, currentAircraft.get().trajectoryProperty()));

        return trajectoryGroup;
    }

    private void drawTrajectory(Group trajectoryGroup,ObservableList<ObservableAircraftState.AirbornePosition> trajView){
        trajectoryGroup.getChildren().clear();

        int zoom = mapParameters.getZoom();
        double minX = mapParameters.getminX();
        double minY = mapParameters.getminY();

        for(int i = 1; i < trajView.size(); ++i) {
            GeoPos pos = trajView.get(i).position();
            GeoPos oldpos = trajView.get(i-1).position();
            double oldX = WebMercator.x(zoom, oldpos.longitude()) - minX - trajectoryGroup.getLayoutX();
            double oldY = WebMercator.y(zoom, oldpos.latitude()) - minY - trajectoryGroup.getLayoutY();
            double newX = WebMercator.x(zoom, pos.longitude()) - minX - trajectoryGroup.getLayoutX();
            double newY = WebMercator.y(zoom, pos.latitude()) - minY - trajectoryGroup.getLayoutY();
            Line newLine = new Line(oldX, oldY, newX, newY);

            double alt = trajView.get(i).altitude();
            double oldAlt = trajView.get(i-1).altitude();
            if(alt == oldAlt){
                newLine.setStroke(plasma.colorAt(Math.pow((alt/12000d) , 1d/3d)));
            }else {
                Stop s0 = new Stop(0,plasma.colorAt(Math.pow((alt/12000d) , 1d/3d)));
                Stop s1 = new Stop(1,plasma.colorAt(Math.pow((oldAlt/12000d) , 1d/3d)));
                LinearGradient linearGradient = new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s0, s1);
                newLine.setStroke(linearGradient);
            }

            trajectoryGroup.getChildren().add(newLine);
        }
    }

    private Group groupForAffichageAircraft(ObservableAircraftState aircraftState){
        Group etiquette = nodeForEtiquette(aircraftState);
        Node icone = nodeForIcone(aircraftState);

        Group affichage = new Group(etiquette,icone);

        affichage.layoutXProperty().bind(
                Bindings.createDoubleBinding(() -> WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) - mapParameters.getminX(),
                        aircraftState.positionProperty(),mapParameters.zoomProperty(),mapParameters.minXProperty()));

        affichage.layoutYProperty().bind(
                Bindings.createDoubleBinding(() -> WebMercator.y(mapParameters.getZoom(), aircraftState.getPosition().latitude()) - mapParameters.getminY(),
                        aircraftState.positionProperty(),mapParameters.zoomProperty(),mapParameters.minYProperty()));


        affichage.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return affichage;
    }

    private Node nodeForIcone(ObservableAircraftState aircraftState){
        AircraftData aircraftData = aircraftState.getAircraftData();
        if (aircraftData == null)return new Group();

        AircraftIcon aircraftIcon = AircraftIcon.iconFor(aircraftData.typeDesignator(),aircraftData.description(),
                                                aircraftState.getCategory(),aircraftData.wakeTurbulenceCategory());

        SVGPath iconSVG = new SVGPath();
        iconSVG.setContent(aircraftIcon.svgPath());
        iconSVG.getStyleClass().add("aircraft");

        iconSVG.rotateProperty().bind(aircraftState.trackOrHeadingProperty().map(dir -> {
            if(aircraftIcon.canRotate()) {
                return Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE);
            }else {
                return 0;
            }
        }));

        iconSVG.fillProperty().bind(aircraftState.altitudeProperty().map(alt -> {
            double colorRampValue = Math.pow((alt.doubleValue()/12000d) , 1d/3d);
            return plasma.colorAt(colorRampValue);
        }));

        iconSVG.setOnMouseClicked(event -> {currentAircraft.set(aircraftState);
            System.out.println("Clicked on : "+aircraftState.getIcaoAddress().string());});

        return iconSVG;
    }

    private Group nodeForEtiquette(ObservableAircraftState aircraftState) {

        Text txtInfo = new Text();

        StringBinding stringBinding = Bindings.createStringBinding(() ->{
            String registration;
                if(aircraftState.getAircraftData() != null) {
                    registration = aircraftState.getAircraftData().registration().string();
                return String.format("%s\n%skm/h\u2002%sm",
                        (registration != null) ?
                                registration :
                                ((aircraftState.getCallSign() != null) ?
                                        aircraftState.getCallSign().string() : aircraftState.getIcaoAddress().string()),
                        (Double.isNaN(aircraftState.getVelocity())) ?
                                "?" : String.valueOf((int)Units.convert(aircraftState.getVelocity(), Units.Speed.KNOT, Units.Speed.KILOMETER_PER_HOUR)),
                        (Double.isNaN(aircraftState.getAltitude())) ?
                                "?" : String.valueOf((int)aircraftState.getAltitude()));
                }else{
                    return String.format("");
                }
            },
            aircraftState.altitudeProperty(),aircraftState.velocityProperty());

        txtInfo.textProperty().bind(stringBinding);

        //création du rectangle dans lequel mettre les infos
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(txtInfo.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(txtInfo.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        //étiquette des infos de l'aéronef
        Group etiquette = new Group(rectangle,txtInfo);
        etiquette.getStyleClass().add("label");
        etiquette.visibleProperty().bind(mapParameters.zoomProperty().map(zoom -> (zoom.intValue() >= 11)));

        //retourne un groupe composé de tout ce qui est nécessaire pour l'étiquette
        return etiquette;
    }

    public Pane pane(){return pane;}
}
