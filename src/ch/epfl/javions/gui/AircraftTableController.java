package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseButton;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Gère la table des aéronefs
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class AircraftTableController {

    //===================================== Attributs privées statiques ================================================
    private static final int OACI_COLUMN_WIDTH = 60;
    private static final int CALLSIGN_COLUMN_WIDTH = 70;
    private static final int IMMAT_COLUMN_WIDTH = 90;
    private static final int MODEL_COLUMN_WIDTH = 230;
    private static final int TYPE_COLUMN_WIDTH = 50;
    private static final int DESCRIPTION_COLUMN_WIDTH = 70;
    private static final int NUMERIC_COLUMN_WIDTH = 85;
    private static final String TABLE_STYLESHEET = "table.css";
    private static final String COLUMN_CLASS_SHEET = "numeric";
    private static final NumberFormat positionNumberFormat = numberFormatOfMaxAndMinDecimal(4);
    private static final NumberFormat otherNumericColumnNumberFormat = numberFormatOfMaxAndMinDecimal(0);


    //===================================== Méthodes privées statiques =================================================
    private static NumberFormat numberFormatOfMaxAndMinDecimal(int nbr){
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(nbr);
        numberFormat.setMinimumFractionDigits(nbr);

        return numberFormat;
    }

    //===================================== Attributs privées ==========================================================
    private final TableView<ObservableAircraftState> tableView;
    private Consumer<ObservableAircraftState> consumer;

    //===================================== Méthodes privées ===========================================================

    private void setSelectedAircraftHandler(ObjectProperty<ObservableAircraftState> currentAircraft){

        //gérer la sélection dans la  table d'un aircraft
        TableView.TableViewSelectionModel<ObservableAircraftState> selectionModel = tableView.getSelectionModel();

        selectionModel.selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> currentAircraft.set(newValue));

        currentAircraft.addListener((observable, oldValue, newValue) -> {
            selectionModel.select(newValue);

            //selectionModel.clearSelection();  --si on veut quil n'y ai plus rien de selectionné


            //pour pas que lors d'un double click il y ai ambiguité sur l'avion sélectionné
            //à cause d'un scroll automatique
            TableViewSkin<?> tableViewSkin = (TableViewSkin<?>) tableView.getSkin();
            VirtualFlow<?> flow = (VirtualFlow<?>) tableViewSkin.getChildren().get(1);
            int newIndex = tableView.getItems().indexOf(newValue);

            //+1 et -1 pour quand meme scroll si on voit qu'un tout petit bout de la ligne
            if (newIndex < flow.getFirstVisibleCell().getIndex() + 1
                    || newIndex > flow.getLastVisibleCell().getIndex() - 1) {
                tableView.scrollTo(newValue);       //ATTENTION
            }
        });

        tableView.setOnMouseClicked(event -> {
            if(Objects.nonNull(currentAircraft.get()) && event.getButton() == MouseButton.PRIMARY &&
                                                                                            event.getClickCount() == 2){
                consumer.accept(currentAircraft.get());
            }
        });
    }

    private TableView<ObservableAircraftState> setUpTable(ObservableSet<ObservableAircraftState> states){

        TableView<ObservableAircraftState> tableView = new TableView<>();

        //colonne de l'OACI
        TableColumn<ObservableAircraftState,String> oaciColumn = getDataColumnOf("OACI", OACI_COLUMN_WIDTH,
                oas -> new ReadOnlyObjectWrapper<>(oas.getIcaoAddress().string()));

        //colonne de l'indicatif
        TableColumn<ObservableAircraftState,String> callsignColumn = getDataColumnOf("Indicatif",
                CALLSIGN_COLUMN_WIDTH,
                oas -> oas.callSignProperty().map(CallSign::string));

        //colonne de l'immatriculation
        TableColumn<ObservableAircraftState,String> registrationColumn = getDataColumnOf("Immatriculation",
                IMMAT_COLUMN_WIDTH,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(data -> data.registration().string()));

        //colonne du modèle
        TableColumn<ObservableAircraftState,String> modelColumn = getDataColumnOf("Modèle", MODEL_COLUMN_WIDTH,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(AircraftData::model));

        //colonne du type
        TableColumn<ObservableAircraftState,String> typeColumn = getDataColumnOf("Type", TYPE_COLUMN_WIDTH,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(data -> data.typeDesignator().string()));

        //colonne de la description
        TableColumn<ObservableAircraftState,String> descriptionColumn = getDataColumnOf("Description",
                DESCRIPTION_COLUMN_WIDTH,
                oas -> new ReadOnlyObjectWrapper<>(oas.getAircraftData()).map(data -> data.description().string()));

        //colonne de la longitude
        TableColumn<ObservableAircraftState,String> longitudeColumn = getNumericColumnOf("Longitude (°)",
                obs -> obs.positionProperty().map(pos ->
                        positionNumberFormat.format(Units.convertTo(pos.longitude(),Units.Angle.DEGREE))));

        //colonne de la latitude
        TableColumn<ObservableAircraftState,String> latitudeColumn = getNumericColumnOf("Latitude (°)",
                obs -> obs.positionProperty().map(pos ->
                        positionNumberFormat.format(Units.convertTo(pos.latitude(),Units.Angle.DEGREE))));

        //colonne de l'altitude
        TableColumn<ObservableAircraftState,String> altitudeColumn = getNumericColumnOf("Altitude (m)",
                obs -> obs.altitudeProperty().map(alt ->
                        (Double.isNaN(alt.doubleValue())) ?
                                "" : otherNumericColumnNumberFormat.format(alt.doubleValue())
                        ));

        //colonne de la vitesse
        TableColumn<ObservableAircraftState,String> speedColumn = getNumericColumnOf("Vitesse (km/h)",
                obs -> obs.velocityProperty().map(speed ->
                                (Double.isNaN(speed.doubleValue())) ?
                                        "" : otherNumericColumnNumberFormat.format
                                                (Units.convertTo(speed.doubleValue(),Units.Speed.KILOMETER_PER_HOUR))));



        //ajoute toutes les colonnes à laa table dans le bon ordre
        tableView.getColumns().addAll(
                List.of(oaciColumn,callsignColumn,registrationColumn,modelColumn,typeColumn,descriptionColumn,
                longitudeColumn,latitudeColumn,altitudeColumn,speedColumn));


        //met à jour la table en fonction de la liste des aéronefs visibles
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if(change.wasAdded()){
                tableView.getItems().add(change.getElementAdded());
                tableView.sort();
            }else {
                tableView.getItems().remove(change.getElementRemoved());
            }
        });

        return tableView;

    }

    //pour les colones non numériques
    private TableColumn<ObservableAircraftState, String> getDataColumnOf
                (String name, int width, Function<ObservableAircraftState, ObservableValue<String>> functionToApply){

        TableColumn<ObservableAircraftState,String> column = new TableColumn<>(name);
        column.setPrefWidth(width);
        column.setCellValueFactory(obs -> functionToApply.apply(obs.getValue()));

       return column;
    }

    //pour les colones numériques
    private TableColumn<ObservableAircraftState, String> getNumericColumnOf
                            (String name, Function<ObservableAircraftState, ObservableValue<String>> functionToApply){

        TableColumn<ObservableAircraftState,String> column = new TableColumn<>(name);
        column.getStyleClass().add(COLUMN_CLASS_SHEET);
        column.setPrefWidth(NUMERIC_COLUMN_WIDTH);

        column.setCellValueFactory(obs -> functionToApply.apply(obs.getValue()));

        column.setComparator((s1, s2) -> {
            try {
                if (s1.isEmpty()||s2.isEmpty()) return s1.compareTo(s2);

                double d1 = positionNumberFormat.parse(s1).doubleValue();
                double d2 = positionNumberFormat.parse(s2).doubleValue();
                return Double.compare(d1,d2);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });

        return column;
    }

    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur
     * @param states : l'ensemble (observable) des états des aéronefs qui doivent apparaître sur la vue.
     * @param currentAicraftState : propriété JavaFX contenant l'état de l'aéronef sélectionné.
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> currentAicraftState){
        tableView = setUpTable(states);

        tableView.getStylesheets().add(TABLE_STYLESHEET);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);

        setSelectedAircraftHandler(currentAicraftState);
    }


    /**
     * Accès au noeud à la racine du graphe du graphe de scène de la classe (table des informations des aéronefs)
     * @return le noeud à la racine de son graphe de scène
     */
    public TableView<ObservableAircraftState> pane(){
        return tableView;
    }

    /**
     * Gère les doubles clics sur la table d'aéronefs (appelle la méthode accept du consumer)
     * @param consumer : information de gestion de l'événement
     */
    //à appeler ainsi :
    //consumer<State> ToDoWithState = (state) -> action à faire
    //setOnDoubleClick(consumer)
    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer){
        this.consumer = consumer;
    }
}
