package ch.epfl.javions.gui;
        import ch.epfl.javions.GeoPos;
        import ch.epfl.javions.Units;
        import ch.epfl.javions.WebMercator;
        import ch.epfl.javions.aircraft.AircraftData;
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
        import javafx.scene.shape.*;
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
    private final static int LABEL_WIDTH = 4;
    private final static double MAX_ALTITUDE_APPROXIMATION = 12000d;
    private final static double EXPONENT_FOR_CUBE_ROOT = 1d/3d;


    //===================================== Attributs privées ==========================================================

    private MapParameters mapParameters;
    private ObservableSet<ObservableAircraftState> states;
    private ObjectProperty<ObservableAircraftState> currentAircraft;
    private Pane pane = new Pane();


    //===================================== Méthodes privées ===========================================================

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
            GeoPos oldpos = trajView.get(i-1).position();
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

    private Group groupForAffichageAircraft(ObservableAircraftState aircraftState){
        Group etiquette = nodeForLabel(aircraftState);
        Node icone = nodeForIcone(aircraftState);
        Group affichage = new Group(etiquette,icone);

        affichage.layoutXProperty().bind(
                Bindings.createDoubleBinding(() ->
                                WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) -
                                        mapParameters.getminX(),
                        aircraftState.positionProperty(),
                        mapParameters.zoomProperty(),
                        mapParameters.minXProperty()));

        affichage.layoutYProperty().bind(
                Bindings.createDoubleBinding(() ->
                                WebMercator.y(mapParameters.getZoom(), aircraftState.getPosition().latitude()) -
                                        mapParameters.getminY(),
                        aircraftState.positionProperty(),
                        mapParameters.zoomProperty(),
                        mapParameters.minYProperty()));

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
        iconSVG.getStyleClass().add(ICON_STYLECLASS);
        iconSVG.rotateProperty().bind(aircraftState.trackOrHeadingProperty().map(dir -> {
            if(aircraftIcon.canRotate()) {
                return Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE);
            }else {
                return 0;
            }
        }));
        iconSVG.fillProperty().bind(aircraftState.altitudeProperty().map(alt -> {
            double colorRampValue = colorRampValue(alt.doubleValue());
            return ColorRamp.PLASMA.colorAt(colorRampValue);
        }));
        iconSVG.setOnMouseClicked(event -> currentAircraft.set(aircraftState));
        return iconSVG;
    }

    private Group nodeForLabel(ObservableAircraftState aircraftState) {
        Text txtInfo = new Text();

        StringBinding stringBinding = Bindings.createStringBinding(() ->{
            String registration;
            if(aircraftState.getAircraftData() != null) {
                registration = aircraftState.getAircraftData().registration().string();
                return String.format(LABEL_TEXT_FORMATTING,
                        (registration != null) ?
                                registration : ((aircraftState.getCallSign() != null) ?
                                aircraftState.getCallSign().string() : aircraftState.getIcaoAddress().string()),
                        (Double.isNaN(aircraftState.getVelocity())) ?
                                UNKNOWN_INFORMATION_TEXT : String.valueOf((int)Units.convertTo(aircraftState.getVelocity(),
                                Units.Speed.KILOMETER_PER_HOUR)),
                        (Double.isNaN(aircraftState.getAltitude())) ?
                                UNKNOWN_INFORMATION_TEXT : String.valueOf((int)aircraftState.getAltitude()));
            }else{
                return UNKNOWN_INFORMATION_TEXT;
            }
        }, aircraftState.altitudeProperty(),aircraftState.velocityProperty());

        txtInfo.textProperty().bind(stringBinding);
        //création du rectangle dans lequel mettre les infos
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(txtInfo.layoutBoundsProperty().map(b -> b.getWidth() + LABEL_WIDTH));
        rectangle.heightProperty().bind(txtInfo.layoutBoundsProperty().map(b -> b.getHeight() + LABEL_WIDTH));
        //étiquette des infos de l'aéronef
        Group etiquette = new Group(rectangle,txtInfo);
        etiquette.getStyleClass().add(LABEL_STYLECLASS);

        etiquette.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                        (mapParameters.zoomProperty().intValue() >= MIN_ZOOM_FOR_LABEL ||
                                (Objects.nonNull(currentAircraft.get()) && currentAircraft.get().equals(aircraftState))),
                mapParameters.zoomProperty(),currentAircraft));

        //retourne un groupe composé de tout ce qui est nécessaire pour l'étiquette
        return etiquette;
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
        this.states = states;
        this.currentAircraft = currentAicraftState;

        pane.setPickOnBounds(false);
        pane.getStylesheets().add(PANE_STYLESHEETS);
        layoutVisibleAircrafts();
    }

    /**
     * Retourne le panneau JavaFX sur lequel les aéronefs sont affichés.
     * Ce panneau est destiné à être superposé à celui montrant le fond de carte.
     * @return le panneau JavaFX sur lequel les aéronefs sont affichés.
     */
    public Pane pane(){return pane;}
}
