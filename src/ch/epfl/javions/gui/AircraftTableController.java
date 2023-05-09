package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.function.Consumer;


/**
 * Gère la table des aéronefs
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class AircraftTableController {

    private static final String TABLE_STYLESHEET = "table.css";
    private ObservableSet<ObservableAircraftState> states;
    private ObjectProperty<ObservableAircraftState> currentAircraft;
    private TableView pane;

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
    }

    private void setUpTable(){
        setUpCollumn("OACI",60);
        setUpCollumn("Indicatif",70);
        setUpCollumn("Immatriculation",90);
        setUpCollumn("Modèle",230);
        setUpCollumn("Type",50);
        setUpCollumn("Description",70);
        setUpCollumn("Longitude (°)");
        setUpCollumn("Latitude (°)");
        setUpCollumn("Altitude (m)");
        setUpCollumn("Vitesse (km/h)");
        //a faire pour chaque collonne

    }

    //pour les collones non numériques
    private void setUpCollumn(String name, int width){
        TableColumn<ObservableAircraftState,String> Collumn = new TableColumn<>(name);
        Collumn.setPrefWidth(width);

        //pas encore sûr qu'il faille ça
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if(change.wasAdded()){
                ObservableAircraftState aircraftState = change.getElementAdded();
                ObservableValue<String> constantObservable = new ReadOnlyObjectWrapper<>("JAVION");
                //Collumn.getColumns().add(0, constantObservable);
            }else{
                //String icaoAdressToRemove = change.getElementRemoved().getIcaoAddress().string();
                //pane.getChildren().removeIf(node -> node.getId().equals(icaoAdressToRemove));
            }
        });

        pane.getColumns().add(Collumn);
    }

    //pour les collones numériques
    private void setUpCollumn(String name){
        TableColumn<ObservableAircraftState,String> Collumn = new TableColumn<>(name);
        Collumn.getStyleClass().add("numeric"); //si donnee numerique
        Collumn.setPrefWidth(85);
        pane.getColumns().add(Collumn);
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
