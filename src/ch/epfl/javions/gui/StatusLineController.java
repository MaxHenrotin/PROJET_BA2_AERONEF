package ch.epfl.javions.gui;

import javafx.beans.property.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public final class StatusLineController {
    private BorderPane pane;
    private IntegerProperty aircraftCountProperty;
    private LongProperty messageCountProperty;

    public StatusLineController(){
        pane = new BorderPane();

        aircraftCountProperty = new SimpleIntegerProperty(0);
        Text aircraftCountText = new Text();
        aircraftCountText.textProperty().bind(aircraftCountProperty.map(count -> "Aéronefs visibles : "+count.toString()));
        pane.setLeft(aircraftCountText);

        messageCountProperty = new SimpleLongProperty(0L);
        Text messageCountText = new Text();
        messageCountText.textProperty().bind(messageCountProperty.map(count -> "Messages reçus : "+count.toString()));
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
