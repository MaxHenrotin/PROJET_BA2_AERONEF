package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
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
    private ObjectProperty<ObservableAircraftState> currentAircraft;

    private ColorRamp plasma;

    private Pane pane;
    public AircraftController(MapParameters mapParameters, ObservableSet<ObservableAircraftState> states, ObjectProperty<ObservableAircraftState> currentAicraftState){
        this.mapParameters = mapParameters;
        this.states = states;
        this.currentAircraft = currentAicraftState;
        //plasma = ColorRamp.PLASMA;

        pane = new Pane();
        pane.setPickOnBounds(false);
        layoutVisibleAircrafts();
    }

    private void layoutVisibleAircrafts(){
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if(change.wasAdded()){
                pane.getChildren().add(groupForAffichageAircraft(change.getElementAdded()));
            }else {
                ObservableList<Node> paneChildrens = pane.getChildren();
                String icaoAdressToRemove = change.getElementRemoved().getIcaoAddress().string();
                for (Node child : paneChildrens) {
                    if(child.getId().equals(icaoAdressToRemove)) paneChildrens.remove(child);
                }
            }
        }
        );
    }

    private Group groupForAircraft(ObservableAircraftState aircraftState){
        Group trajectoryGroup = new Group();
        Group afficheAircraft = groupForAffichageAircraft(aircraftState);

        Group annotatedAircraft = new Group(trajectoryGroup,afficheAircraft);
        annotatedAircraft.setId(aircraftState.getIcaoAddress().string());

        return annotatedAircraft;
    }

    private Group groupForAffichageAircraft(ObservableAircraftState aircraftState){
        Group etiquette = nodeForEtiquette(aircraftState);
        Node icone = nodeForIcone(aircraftState);

        Group affichage = new Group(etiquette,icone);

        //DoubleBinding posXBinding = Bindings.createDoubleBinding(() -> WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) - mapParameters.getminX(),aircraftState.positionProperty());
        affichage.layoutXProperty().bind(aircraftState.positionProperty().map(pos ->
            WebMercator.x(mapParameters.getZoom(), pos.longitude()) - mapParameters.getminX()
        ));
        affichage.layoutYProperty().bind(aircraftState.positionProperty().map(pos ->
                WebMercator.y(mapParameters.getZoom(), pos.latitude()) - mapParameters.getminY()
        ));

        affichage.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return affichage;
    }

    private Node nodeForIcone(ObservableAircraftState aircraftState){
        AircraftData aircraftData = aircraftState.getAircraftData();

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

        /*iconSVG.fillProperty().bind(aircraftState.altitudeProperty().map(alt -> {
            double colorRampValue = Math.pow((alt.doubleValue()/12000d) , 1d/3d);
            return plasma.colorAt(colorRampValue);
        }));*/
        iconSVG.setFill(Color.DARKGREY);


        return iconSVG;
    }

    private Group nodeForEtiquette(ObservableAircraftState aircraftState) {

        Text info = new Text();
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

        info.textProperty().bind(stringBinding);

        //création du rectangle dans lequel mettre les infos
        Rectangle rectangle = new Rectangle(10,10,Color.TRANSPARENT);
        rectangle.widthProperty().bind(info.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(info.layoutBoundsProperty().map(b -> b.getHeight() +4));

        //étiquette des infos de l'aéronef
        Group etiquette = new Group(rectangle, info);
        etiquette.getStyleClass().add("label");
        etiquette.visibleProperty().bind(mapParameters.zoomProperty().map(zoom -> (zoom.intValue() >= 11)));

        //retourne un groupe composé de tout ce qui est nécessaire pour l'étiquette
        return etiquette;
    }

    public Pane pane(){return pane;}
}
