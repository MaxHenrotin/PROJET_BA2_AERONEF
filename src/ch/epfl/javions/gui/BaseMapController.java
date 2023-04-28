package ch.epfl.javions.gui;


import ch.epfl.javions.GeoPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;

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

    public Pane pane() throws IOException {
        Canvas canvas = new Canvas();
        Pane pane = new Pane(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        double minX = mapParameters.getminX();
        double minY = mapParameters.getminY();
        double maxX = minX + canvas.getWidth();
        double maxY = minY + canvas.getHeight();
        int zoom = mapParameters.getZoom();

        TileManager.TileId[][] tabOfTileId = initTable(minX,minY,maxX,maxY,zoom);

        for (int i = 0; i < tabOfTileId.length; i++) {
            for (int j = 0; j < tabOfTileId[0].length; j++) {
                graphicsContext.drawImage(tileManager.imageOfTile(tabOfTileId[i][j]),i,j);
            }
        }

        return pane;
    }

    private int coordonneesToTileIndex(double coord){
        return (int) Math.floor(coord / 256d);
    }

    private TileManager.TileId[][] initTable(double minX,double minY,double maxX,double maxY,int zoom){
        int minXTileId = coordonneesToTileIndex(minX);
        int minYTileId = coordonneesToTileIndex(minY);
        int maxXTileId = coordonneesToTileIndex(maxX);
        int maxYTileId = coordonneesToTileIndex(maxY);

        TileManager.TileId[][] tabOfTileId = new TileManager.TileId[maxXTileId-minXTileId][maxYTileId-minYTileId];

        for (int i = 0; i < tabOfTileId.length; i++) {
            for (int j = 0; j < tabOfTileId[0].length; j++) {
                tabOfTileId[i][j] = new TileManager.TileId(zoom,minXTileId+i,minYTileId+j);
            }
        }
        return tabOfTileId;
    }
    public void centerOn(GeoPos newCenter){

    }
}
