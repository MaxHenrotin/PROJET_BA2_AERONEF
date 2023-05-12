package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Gère la table des aéronefs
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class AircraftTableController {

    private static final String TABLE_STYLESHEET = "table.css";
    private final ObservableSet<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> currentAircraft;
    private final TableView pane;



    private void selectAircraftHandler(){
        //gérer le clic

        TableView.TableViewSelectionModel selectionModel = pane.getSelectionModel();

        selectionModel.selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> currentAircraft.set((ObservableAircraftState) newValue));

        /*currentAircraft.addListener(observable -> {
            selectionModel.select(observable);
            SimpleObjectProperty<ObservableAircraftState> selectedAircraft = (SimpleObjectProperty<ObservableAircraftState>) selectionModel.getSelectedItem();
            if(!Objects.equals(selectedAircraft,currentAircraft)) pane.scrollTo(selectedAircraft);
        });*/
    }

    private void setUpTable(){
        ObservableList columns = pane.getColumns();

        NumberFormat positionNumberFormat = numberFormatOfMaxAndMinDecimal(4);
        NumberFormat otherNumericCollumnNumberFormat = numberFormatOfMaxAndMinDecimal(0);

        TableColumn<ObservableAircraftState,String> icaoColumn = getDataCollumnOf("OACI",60,
                oas -> new ReadOnlyObjectWrapper<>(oas.getIcaoAddress().string()));

        TableColumn<ObservableAircraftState,String> indicatifCollumn = getDataCollumnOf("Indicatif",70,
                oas -> oas.callSignProperty().map(CallSign::string));

        TableColumn<ObservableAircraftState,String> immatCollumn = getDataCollumnOf("Immatriculation",90,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(data -> data.registration().string()));

        TableColumn<ObservableAircraftState,String> modeleCollumn = getDataCollumnOf("Modèle",230,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(AircraftData::model));

        TableColumn<ObservableAircraftState,String> typeCollumn = getDataCollumnOf("Type",50,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(data -> data.typeDesignator().string()));

        TableColumn<ObservableAircraftState,String> descriptionCollumn = getDataCollumnOf("Description",70,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(data -> data.description().string()));

        TableColumn<ObservableAircraftState,String> longitudeCollumn = getNumericCollumnOf("Longitude (°)",
                obs -> obs.positionProperty().map(pos ->
                        positionNumberFormat.format(Units.convertTo(pos.longitude(),Units.Angle.DEGREE))));

        TableColumn<ObservableAircraftState,String> latitudeCollumn = getNumericCollumnOf("Latitude (°)",
                obs -> obs.positionProperty().map(pos ->
                        positionNumberFormat.format(Units.convertTo(pos.latitude(),Units.Angle.DEGREE))));

        TableColumn<ObservableAircraftState,String> altCollumn = getNumericCollumnOf("Altitude (m)",
                obs -> obs.altitudeProperty().map(alt ->
                        otherNumericCollumnNumberFormat.format(alt.doubleValue())));

        TableColumn<ObservableAircraftState,String> speedCollumn = getNumericCollumnOf("Vitesse (km/h)",
                obs -> obs.velocityProperty().map(speed ->
                        otherNumericCollumnNumberFormat.
                                format(
                                        Units.convertTo(speed.doubleValue(),Units.Speed.KILOMETER_PER_HOUR))));



        columns.addAll(icaoColumn,indicatifCollumn,immatCollumn,modeleCollumn,typeCollumn,descriptionCollumn,
                longitudeCollumn,latitudeCollumn,altCollumn,speedCollumn);
    }

    //pour les collones non numériques
    private TableColumn<ObservableAircraftState, String> getDataCollumnOf(String name, int width,
                                                                          Function<ObservableAircraftState, ObservableValue<String>> functionToApply){
        TableColumn<ObservableAircraftState,String> collumn = new TableColumn<>(name);
        collumn.setPrefWidth(width);
        collumn.setCellValueFactory(obs -> functionToApply.apply(obs.getValue()));

       return collumn;
    }

    //pour les collones numériques
    private TableColumn<ObservableAircraftState, String> getNumericCollumnOf(String name, Function<ObservableAircraftState, ObservableValue<String>> functionToApply){
        TableColumn<ObservableAircraftState,String> collumn = new TableColumn<>(name);
        collumn.getStyleClass().add("numeric");
        collumn.setPrefWidth(85);
        collumn.setCellValueFactory(obs -> functionToApply.apply(obs.getValue()));

        return collumn;
    }

    private NumberFormat numberFormatOfMaxAndMinDecimal(int nbr){
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(nbr);
        numberFormat.setMinimumFractionDigits(nbr);

        return numberFormat;
    }
    /**
     * Constructeur
     * @param states : l'ensemble (observable) des états des aéronefs qui doivent apparaître sur la vue.
     * @param currentAicraftState : propriété JavaFX contenant l'état de l'aéronef sélectionné.
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> currentAicraftState){
        this.states = states;
        this.currentAircraft = currentAicraftState;

        pane = new TableView();
        pane.getStylesheets().add(TABLE_STYLESHEET);
        pane.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        pane.setTableMenuButtonVisible(true);

        setUpTable();

        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if(change.wasAdded()){
                pane.getItems().add(change.getElementAdded());
            }else {
                pane.getItems().remove(change.getElementRemoved());
            }
        });


    }


    /**
     * Accès au noeud à la racine du graphe du graphe de scène de la classe (table des informations des aéronefs)
     * @return le noeud à la racine de son graphe de scène
     */
    public TableView pane(){
        return pane;
    }

    /**
     * Gère les doubles clics sur la table d'aéronefs (appelle la méthode accept du consumer)
     * @param consumer : information de gestion de l'événement
     */
    //à appeler ainsi :
    //consumer<State> ToDoWithState = (state) -> action à faire
    //setOnDoubleClick(consumer)
    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer){
        consumer.accept(currentAircraft.get());
    }

}
