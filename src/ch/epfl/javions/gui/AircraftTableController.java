package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
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
