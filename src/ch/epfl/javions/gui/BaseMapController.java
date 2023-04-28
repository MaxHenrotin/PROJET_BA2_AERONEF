package ch.epfl.javions.gui;


import ch.epfl.javions.GeoPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

/**
 * Classe publique finale gérant l'affichage et l'interaction avec le fond de carte
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public class BaseMapController {

    private MapParameters mapParameters;
    private TileManager tileManager;


    public BaseMapController(TileManager tileManager,MapParameters mapParameters){
        this.mapParameters = mapParameters;
        this.tileManager = tileManager;

    }

    public Pane pane(Canvas canvas){
        Pane pane = new Pane();
        canvas.widthProperty().bind(pane.widthProperty());
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();


        return new Pane();
    }

    private int coordonneesToTileIndex(double coord){
        return (int) Math.floor(coord / 256d);
    }
    public void centerOn(GeoPos newCenter){

    }
}
