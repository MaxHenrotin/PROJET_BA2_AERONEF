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
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.util.List;


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
                ObservableList<Node> paneChildrens = pane.getChildren();
                String icaoAdressToRemove = change.getElementRemoved().getIcaoAddress().string();

                for (Node child : paneChildrens) {
                    if(child.getId().equals(icaoAdressToRemove)) paneChildrens.remove(child);
                }
            }
        });
    }

    private Group groupForAircraft(ObservableAircraftState aircraftState){
        Path trajectoryGroup = groupForTrajectory();
        Group afficheAircraft = groupForAffichageAircraft(aircraftState);

        Group annotatedAircraft = new Group(trajectoryGroup,afficheAircraft);
        annotatedAircraft.setId(aircraftState.getIcaoAddress().string());

        return annotatedAircraft;
    }

    private Path groupForTrajectory(){

        Path trajectoryGroup = new Path();
        trajectoryGroup.getStyleClass().add("trajectory");
        trajectoryGroup.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentAircraft.get() != null,currentAircraft,mapParameters.zoomProperty()));

        int zoom = mapParameters.getZoom();
        double minX = mapParameters.getminX();
        double minY = mapParameters.getminY();

        if(trajectoryGroup.isVisible()){
            ObservableList<ObservableAircraftState.AirbornePosition> trajView = currentAircraft.get().trajectoryProperty();
            trajView.addListener((ListChangeListener<ObservableAircraftState.AirbornePosition>) change -> {
                GeoPos pos = change.getList().get(0).position();
                LineTo lineTo = new LineTo(WebMercator.x(zoom,pos.longitude())-minX,WebMercator.y(zoom, pos.latitude())-minY);
                trajectoryGroup.getElements().add(lineTo);
                trajectoryGroup.setStroke(Color.RED);
            });
        }

/*
        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");
        trajectoryGroup.visibleProperty().bind(Bindings.createBooleanBinding(() -> currentAircraft.get() != null,currentAircraft,mapParameters.zoomProperty()));
        ObservableList<ObservableAircraftState.AirbornePosition> traj = aircraftState.trajectoryProperty();
        Path path = new Path();
        GeoPos startPos = traj.get(0).position();
        int zoom = mapParameters.getZoom();
        double minX = mapParameters.getminX();
        double minY = mapParameters.getminY();
        MoveTo moveTo = new MoveTo(WebMercator.x(zoom,startPos.longitude()) - minX,WebMercator.y(zoom,startPos.latitude()-minY));
        path.getElements().add(moveTo);
        if(!trajectoryGroup.isVisible()){
            traj.addListener((ListChangeListener<ObservableAircraftState.AirbornePosition>) change -> {
                GeoPos pos = change.getList().get(0).position();
                LineTo lineTo = new LineTo(WebMercator.x(zoom,pos.longitude())-minX,WebMercator.y(zoom, pos.latitude())-minY);
                path.getElements().add(lineTo);
            });
            trajectoryGroup.getChildren().add(path);
        }*/

        return trajectoryGroup;
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
                if(aircraftState.getAircraftData() == null){
                    registration = null;
                }else {
                    registration = aircraftState.getAircraftData().registration().string();
                }
                return String.format("%s\n%skm/h\u2002%sm",
                        (registration != null) ?
                                registration :
                                ((aircraftState.getCallSign() != null) ?
                                        aircraftState.getCallSign().string() : aircraftState.getIcaoAddress().string()),
                        (Double.isNaN(aircraftState.getVelocity())) ?
                                "?" : String.valueOf((int)Units.convert(aircraftState.getVelocity(), Units.Speed.KNOT, Units.Speed.KILOMETER_PER_HOUR)),
                        (Double.isNaN(aircraftState.getAltitude())) ?
                                "?" : String.valueOf((int)aircraftState.getAltitude()));},
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
