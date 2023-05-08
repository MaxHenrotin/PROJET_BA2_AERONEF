package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

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
    }

    private void implementCollumns(){
        TableColumn<ObservableAircraftState,String> OACICollumn = new TableColumn<>("OACI");
        //OACICollumn.getStyleClass().add("numeric"); //si donnee numerique
        OACICollumn.setPrefWidth(60);
        pane.getColumns().add(OACICollumn);

        //pas encore sur quil faille ça
        states.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if(change.wasAdded()){
                //pane.getChildren().add(groupForAircraft(change.getElementAdded()));
            }else{
                //String icaoAdressToRemove = change.getElementRemoved().getIcaoAddress().string();
                //pane.getChildren().removeIf(node -> node.getId().equals(icaoAdressToRemove));
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
