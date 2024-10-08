package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.Objects;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;

/**
 * Classe gérant la vue des aéronefs.
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class AircraftController {

    //===================================== Attributs privées statiques ================================================

    private final static String UNKNOWN_INFORMATION_TEXT = "?";
    private final static String PANE_STYLESHEETS = "aircraft.css";
    private final static String TRAJECTORY_STYLECLASS = "trajectory";
    private final static String ICON_STYLECLASS = "aircraft";
    private final static String LABEL_STYLECLASS = "label";
    private final static String LABEL_TEXT_FORMATTING = "%s\n%skm/h\u2002%sm";
    private final static int MIN_ZOOM_FOR_LABEL = 11;
    private final static int LABEL_SIZE_FORMAT = 4;
    private final static double MAX_ALTITUDE_APPROXIMATION = 12000d;
    private final static double EXPONENT_FOR_CUBE_ROOT = 1d/3d;


    //===================================== Attributs privées ==========================================================

    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> currentAircraft;
    private final Pane pane;


    //===================================== Méthodes privées ===========================================================

    private Pane layoutVisibleAircrafts(ObservableSet<ObservableAircraftState> states){
        Pane pane = new Pane();
        //affiche sur la carte tous les avions visibles
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if(change.wasAdded()){
                pane.getChildren().add(groupForAircraft(change.getElementAdded()));
            }else{
                String icaoAdressToRemove = change.getElementRemoved().getIcaoAddress().string();
                pane.getChildren().removeIf(node -> node.getId().equals(icaoAdressToRemove));
            }
        });

        return pane;
    }

    private Group groupForAircraft(ObservableAircraftState aircraftState){
        Group trajectoryGroup = groupForTrajectory(aircraftState);
        Group informationsLabelGroup = groupForInformationsLabel(aircraftState);

        Group annotatedAircraft = new Group(trajectoryGroup,informationsLabelGroup);

        annotatedAircraft.setId(aircraftState.getIcaoAddress().string());

        return annotatedAircraft;
    }

    private Group groupForTrajectory(ObservableAircraftState aircraftState){
        Group trajectoryGroup = new Group();

        trajectoryGroup.getStyleClass().add(TRAJECTORY_STYLECLASS);
        trajectoryGroup.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                Objects.nonNull(currentAircraft.get()) && aircraftState.equals(currentAircraft.get()),currentAircraft));

        trajectoryGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectoryGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());

        aircraftState.trajectoryProperty().addListener(
                (ListChangeListener<ObservableAircraftState.AirbornePosition>) c ->
                        drawTrajectory(trajectoryGroup, aircraftState.trajectoryProperty()));

        mapParameters.zoomProperty().addListener((observable, oldValue, newValue) ->
                drawTrajectory(trajectoryGroup, aircraftState.trajectoryProperty()));

        trajectoryGroup.visibleProperty().addListener((observable, oldValue, newValue) ->
                drawTrajectory(trajectoryGroup, currentAircraft.get().trajectoryProperty()));

        return trajectoryGroup;
    }

    private void drawTrajectory(Group trajectoryGroup,
                                ObservableList<ObservableAircraftState.AirbornePosition> trajView){

        trajectoryGroup.getChildren().clear();

        int zoom = mapParameters.getZoom();
        double minX = mapParameters.getminX();
        double minY = mapParameters.getminY();

        for(int i = 1; i < trajView.size(); ++i) {
            GeoPos pos = trajView.get(i).position();

            //créé des problèmes induit de setAltitude (programme plus performant ???)
            //GeoPos oldpos = trajView.get(i-1).position();

            GeoPos oldpos = Objects.isNull(trajView.get(i-1).position()) ? pos : trajView.get(i-1).position();

            double oldX = WebMercator.x(zoom, oldpos.longitude()) - minX - trajectoryGroup.getLayoutX();
            double oldY = WebMercator.y(zoom, oldpos.latitude()) - minY - trajectoryGroup.getLayoutY();
            double newX = WebMercator.x(zoom, pos.longitude()) - minX - trajectoryGroup.getLayoutX();
            double newY = WebMercator.y(zoom, pos.latitude()) - minY - trajectoryGroup.getLayoutY();

            Line newLine = new Line(oldX, oldY, newX, newY);

            double alt = trajView.get(i).altitude();
            double oldAlt = trajView.get(i-1).altitude();

            //pour la couleur de la trajectoire
            if(alt == oldAlt){
                newLine.setStroke(ColorRamp.PLASMA.colorAt(colorRampValue(alt)));
            }else {
                newLine.setStroke(linearGradientOf(oldAlt,alt));
            }
            trajectoryGroup.getChildren().add(newLine);
        }
    }

    private Group groupForInformationsLabel(ObservableAircraftState aircraftState){
        Group label = nodeForLabel(aircraftState);
        Node icone = nodeForIcone(aircraftState);

        Group layoutAircraft = new Group(label,icone);

        layoutAircraft.layoutXProperty().bind(
                Bindings.createDoubleBinding(() ->
                                WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) -
                                        mapParameters.getminX(),
                        aircraftState.positionProperty(),
                        mapParameters.zoomProperty(),
                        mapParameters.minXProperty()));

        layoutAircraft.layoutYProperty().bind(
                Bindings.createDoubleBinding(() ->
                                WebMercator.y(mapParameters.getZoom(), aircraftState.getPosition().latitude()) -
                                        mapParameters.getminY(),
                        aircraftState.positionProperty(),
                        mapParameters.zoomProperty(),
                        mapParameters.minYProperty()));

        layoutAircraft.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return layoutAircraft;
    }

    private Node nodeForIcone(ObservableAircraftState aircraftState){
        AircraftData aircraftData = aircraftState.getAircraftData();
        AircraftIcon aircraftIcon;

        if (aircraftData == null) {
            aircraftIcon = AircraftIcon.iconFor( new AircraftTypeDesignator(""),new AircraftDescription(""),
                                                    aircraftState.getCategory(), WakeTurbulenceCategory.UNKNOWN);
        }else {
            aircraftIcon = AircraftIcon.iconFor(aircraftData.typeDesignator(),aircraftData.description(),
                                                    aircraftState.getCategory(),aircraftData.wakeTurbulenceCategory());
        }

        SVGPath iconSVG = new SVGPath();
        iconSVG.setContent(aircraftIcon.svgPath());
        iconSVG.getStyleClass().add(ICON_STYLECLASS);

        //rotation de l'icone en fonction de la direction de l'avion
        iconSVG.rotateProperty().bind(aircraftState.trackOrHeadingProperty().map(dir -> (
                aircraftIcon.canRotate()) ?
                            Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE) : 0));

        //couleur de l'icone en fonction de l'altitude
        iconSVG.fillProperty().bind(aircraftState.altitudeProperty().map(alt -> {
            double colorRampValue = colorRampValue(alt.doubleValue());
            return ColorRamp.PLASMA.colorAt(colorRampValue);
        }));

        //séléction de l'avion quand on clique dessus
        iconSVG.setOnMouseClicked(event -> currentAircraft.set(aircraftState));

        return iconSVG;
    }

    //Si on voulait mettre à jour l'Icone d'un aicraft pendant le vol on devrait "écouter" la categoryProperty
    //comme le suggère le bout de code ci dessous
    /*private ObservableValue<AircraftIcon> aircraftIconFor(ObservableAircraftState aircraftState){
        aircraftState.categoryProperty().addListener(
                (category) ->
                 (aircraftState.getAircraftData() != null) ?
                                 AircraftIcon.iconFor(aircraftState.getAircraftData().typeDesignator(),
                                        aircraftState.getAircraftData().description(),
                                        aircraftState.getCategory(),
                                        aircraftState.getAircraftData().wakeTurbulenceCategory()) :
                                AircraftIcon.iconFor(new AircraftTypeDesignator(""),
                                        new AircraftDescription(""),
                                        aircraftState.getCategory(),
                                        WakeTurbulenceCategory.UNKNOWN)););
    }*/

    private Group nodeForLabel(ObservableAircraftState aircraftState) {
        Text txtInfo = new Text();

        StringBinding stringBinding = Bindings.createStringBinding(() ->{
            String registration;
            if(aircraftState.getAircraftData() != null) {
                registration = aircraftState.getAircraftData().registration().string();
            }else {
                registration = null;
            }

            return String.format(LABEL_TEXT_FORMATTING,
                    (registration != null) ?
                            registration : ((aircraftState.getCallSign() != null) ?
                            aircraftState.getCallSign().string() : aircraftState.getIcaoAddress().string()),
                    (Double.isNaN(aircraftState.getVelocity())) ?
                            UNKNOWN_INFORMATION_TEXT : String.valueOf((int)Units.convertTo(aircraftState.getVelocity(),
                            Units.Speed.KILOMETER_PER_HOUR)),
                    (Double.isNaN(aircraftState.getAltitude())) ?
                            UNKNOWN_INFORMATION_TEXT : String.valueOf((int)aircraftState.getAltitude()));

        }, aircraftState.altitudeProperty(),aircraftState.velocityProperty());
        txtInfo.textProperty().bind(stringBinding);

        //création du rectangle dans lequel mettre les infos
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(txtInfo.layoutBoundsProperty().map(b -> b.getWidth() + LABEL_SIZE_FORMAT));
        rectangle.heightProperty().bind(txtInfo.layoutBoundsProperty().map(b -> b.getHeight() + LABEL_SIZE_FORMAT));

        //étiquette des infos de l'aéronef
        Group label = new Group(rectangle,txtInfo);
        label.getStyleClass().add(LABEL_STYLECLASS);

        label.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                        (mapParameters.zoomProperty().intValue() >= MIN_ZOOM_FOR_LABEL ||
                                (Objects.nonNull(currentAircraft.get()) && currentAircraft.get().equals(aircraftState))),
                mapParameters.zoomProperty(),currentAircraft));

        //retourne un groupe composé de tout ce qui est nécessaire pour l'étiquette
        return label;
    }

    private double colorRampValue(double altitude){
        return Math.pow((altitude/MAX_ALTITUDE_APPROXIMATION) , EXPONENT_FOR_CUBE_ROOT);
    }

    /**
     * Cette méthode utilise diverses constantes suivant la convention donnée par l'énoncé du projet à l'étape 9.
     */
    private LinearGradient linearGradientOf(double oldAlt, double alt){
        Stop s0 = new Stop(0, ColorRamp.PLASMA.colorAt(colorRampValue(alt)));
        Stop s1 = new Stop(1, ColorRamp.PLASMA.colorAt(colorRampValue(oldAlt)));
        return new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s0, s1);
    }


    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur
     * @param mapParameters : les paramètres de la portion de la carte visible à l'écran.
     * @param states : l'ensemble (observable) des états des aéronefs qui doivent apparaître sur la vue.
     * @param currentAicraftState : propriété JavaFX contenant l'état de l'aéronef sélectionné.
     */
    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> states,
                              ObjectProperty<ObservableAircraftState> currentAicraftState){
        this.mapParameters = mapParameters;
        this.currentAircraft = currentAicraftState;

        pane = layoutVisibleAircrafts(states);
        pane.setPickOnBounds(false);
        pane.getStylesheets().add(PANE_STYLESHEETS);
    }

    /**
     * Retourne le panneau JavaFX sur lequel les aéronefs sont affichés.
     * Ce panneau est destiné à être superposé à celui montrant le fond de carte.
     * @return le panneau JavaFX sur lequel les aéronefs sont affichés.
     */
    public Pane pane(){return pane;}
}
