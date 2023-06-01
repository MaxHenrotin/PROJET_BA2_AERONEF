package ch.epfl.javions.gui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * Gère la ligne d'état
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class StatusLineController {

    //===================================== Attributs privées statiques ================================================

    private static final String PANE_STYLESHEETS = "status.css";
    private static final String TEXT_FOR_AERONEF_COUNT = "Aéronefs visibles : ";
    private static final String TEXT_FOR_MESSAGE_COUNT = "Messages reçus : ";


    //===================================== Attributs privées ==========================================================

    private final BorderPane pane;
    private final IntegerProperty aircraftCountProperty;
    private final LongProperty messageCountProperty;


    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur par défaut qui construit le graphe de scène
     */
    public StatusLineController(){
        pane = new BorderPane();
        pane.getStylesheets().add(PANE_STYLESHEETS);

        //Instancie la propriétée des aéronefs visbles et le texte associé en les liant
        aircraftCountProperty = new SimpleIntegerProperty(0);
        Text aircraftCountText = new Text();
        aircraftCountText.textProperty().bind(
                aircraftCountProperty.map(count -> TEXT_FOR_AERONEF_COUNT + count.toString()));

        //Ajoute le texte à gauche du panneau
        pane.setLeft(aircraftCountText);

        //Instancie la propriétée des messages reçus et le texte associé en les liant
        messageCountProperty = new SimpleLongProperty(0L);
        Text messageCountText = new Text();
        messageCountText.textProperty().bind(
                messageCountProperty.map(count -> TEXT_FOR_MESSAGE_COUNT + count.toString()));

        //Ajoute le texte à droite du panneau
        pane.setRight(messageCountText);
    }

    /**
     * Retourne le panneau contenant la ligne d'état
     * @return : retourne le panneau contenant la ligne d'état
     */
    public Pane pane(){ return pane; }

    /**
     * Retourne la propriété (modifiable) contenant le nombre d'aéronefs actuellement visibles
     * @return : la propriété contenant le nombre d'aéronefs actuellement visibles
     */
    public IntegerProperty aircraftCountPropertyProperty() { return aircraftCountProperty; }

    /**
     * Retourne la propriété (modifiable) contenant le nombre de messages
     * reçus depuis le début de l'exécution du programme
     * @return : la propriété contenant le nombre de messages reçus depuis le début de l'exécution du programme
     */
    public LongProperty messageCountProperty() { return messageCountProperty; }
}
