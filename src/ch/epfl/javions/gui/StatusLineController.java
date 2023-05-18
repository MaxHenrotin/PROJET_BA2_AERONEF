package ch.epfl.javions.gui;

import javafx.beans.property.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public final class StatusLineController {

    //===================================== Attributs privées statiques ================================================

    private static final String PANE_STYLESHEETS = "status.css";
    private static final String TEXT_FOR_AERONEF_COUNT = "Aéronefs visibles : ";
    private static final String TEXT_FOR_MESSAGE_COUNT = "Messages reçus : ";


    //===================================== Attributs privées ==========================================================

    private BorderPane pane;
    private IntegerProperty aircraftCountProperty;
    private LongProperty messageCountProperty;


    //===================================== Méthodes publiques =========================================================

    public StatusLineController(){
        pane = new BorderPane();
        pane.getStylesheets().add(PANE_STYLESHEETS);

        aircraftCountProperty = new SimpleIntegerProperty(0);
        Text aircraftCountText = new Text();
        aircraftCountText.textProperty().bind(
                aircraftCountProperty.map(count -> TEXT_FOR_AERONEF_COUNT + count.toString()));
        pane.setLeft(aircraftCountText);

        messageCountProperty = new SimpleLongProperty(0L);
        Text messageCountText = new Text();
        messageCountText.textProperty().bind(
                messageCountProperty.map(count -> TEXT_FOR_MESSAGE_COUNT + count.toString()));
        pane.setRight(messageCountText);
    }

    public Pane pane(){
        return pane;
    }

    public IntegerProperty aircraftCountPropertyProperty() {
        return aircraftCountProperty;
    }

    public LongProperty messageCountProperty() {
        return messageCountProperty;
    }
}
