package ch.epfl.javions.gui;


import ch.epfl.javions.GeoPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Arrays;

/**
 * Classe publique finale gérant l'affichage et l'interaction avec le fond de carte
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public class BaseMapController {

    private MapParameters mapParameters;
    private TileManager tileManager;

    Canvas canvas;
    Pane pane;
    public BaseMapController(TileManager tileManager,MapParameters mapParameters){
        this.mapParameters = mapParameters;
        this.tileManager = tileManager;

        canvas = new Canvas();
        pane = new Pane(canvas);
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> {GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
            try {
                graphicsContext.drawImage(tileManager.imageOfTile(new TileManager.TileId(0,0,0)),0,0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        canvas.heightProperty().addListener((observable, oldValue, newValue) -> {GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
            try {
                graphicsContext.drawImage(tileManager.imageOfTile(new TileManager.TileId(0,0,0)),0,0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Pane pane() throws IOException {

        System.out.println("canvas dimension : "+canvas.getWidth()+" , "+canvas.getHeight());

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        double minX = mapParameters.getminX();
        double minY = mapParameters.getminY();
        double maxX = minX + canvas.getWidth();
        double maxY = minY + canvas.getHeight();
        int zoom = mapParameters.getZoom();

        TileManager.TileId[][] tabOfTileId = initTable(minX,minY,maxX,maxY,zoom);

        /*for (int i = 0; i < tabOfTileId.length; i++) {
            for (int j = 0; j < tabOfTileId[0].length; j++) {
                graphicsContext.drawImage(tileManager.imageOfTile(tabOfTileId[i][j]),i,j);
            }
        }*/

        System.out.println("min X :"+coordonneesToTileIndex(minX));
        System.out.println("min Y :"+coordonneesToTileIndex(minY));

        //graphicsContext.drawImage(tileManager.imageOfTile(tabOfTileId[0][0]),0,0);
        graphicsContext.drawImage(tileManager.imageOfTile(new TileManager.TileId(0,0,0)),0,0);

        //System.out.println(tileManager.imageOfTile(new TileManager.TileId(zoom,coordonneesToTileIndex(minX),coordonneesToTileIndex(minY)))==null);

        System.out.println("canvas dimension after : "+canvas.getWidth()+" , "+canvas.getHeight());


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
