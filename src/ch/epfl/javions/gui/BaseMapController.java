package ch.epfl.javions.gui;


import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * Classe publique finale gérant l'affichage et l'interaction avec le fond de carte
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public class BaseMapController {

    public static final double TILE_SIZE_IN_PIXEL = 256d;
    private MapParameters mapParameters;
    private final TileManager tileManager;

    private final Canvas canvas;

    private final Pane pane;

    private boolean redrawNeeded = true;


    public BaseMapController(TileManager tileManager,MapParameters mapParameters){
        this.mapParameters = mapParameters;
        this.tileManager = tileManager;

        canvas = new Canvas();
        pane = new Pane(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.widthProperty().addListener((observable, oldValue, newValue) -> redrawOnNextPulse());

        canvas.heightProperty().addListener((observable, oldValue, newValue) -> redrawOnNextPulse());

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);




            // … à faire : appeler les méthodes de MapParameters

            double deltaX = e.getX();
            double deltaY = e.getY();
            mapParameters.scroll(deltaX,deltaY);
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-deltaX,-deltaY);
            System.out.println("zoom : "+mapParameters.getZoom());
            redrawOnNextPulse();
        });

        SimpleObjectProperty<Point2D> mouseCoordinate = new SimpleObjectProperty<>();

        pane.setOnMousePressed(event -> {
            mouseCoordinate.set(new Point2D.Double(event.getX(),event.getY()));
        });

        pane.setOnMouseDragged(event ->{
            mapParameters.scroll(mouseCoordinate.get().getX() - event.getX(), mouseCoordinate.get().getY() - event.getY());
            mouseCoordinate.set(new Point2D.Double(event.getX(),event.getY()));
            redrawOnNextPulse();
        });

        pane.setOnMouseReleased(event -> redrawOnNextPulse());

    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
    private void redrawIfNeeded(){
        if (!redrawNeeded) return;
        redrawNeeded = false;
        redraw();
    }

    private void redraw() {

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();

        double minX = mapParameters.getminX();
        double minY = mapParameters.getminY();
        double maxX = minX + canvas.getWidth() + TILE_SIZE_IN_PIXEL;
        double maxY = minY + canvas.getHeight() + TILE_SIZE_IN_PIXEL;
        int zoom = mapParameters.getZoom();

        int decalageX = (int) (minX % TILE_SIZE_IN_PIXEL);
        int decalageY = (int) (minY % TILE_SIZE_IN_PIXEL);

        TileManager.TileId[][] tabOfTileId = tabOfTileId(minX,minY,maxX,maxY,zoom);


        for (int i = 0; i < tabOfTileId.length; i++) {
            for (int j = 0; j < tabOfTileId[0].length; j++) {
                try {
                    graphicsContext.drawImage(tileManager.imageOfTile(tabOfTileId[i][j]), TILE_SIZE_IN_PIXEL * i - decalageX, TILE_SIZE_IN_PIXEL * j - decalageY);
                }catch (IOException e) {
                    //fait rien
                }
            }
        }
    }

    public Pane pane() { return pane; }

    private int coordonneesToTileIndex(double coord){
        return (int) Math.floor(coord / 256d);
    }

    private TileManager.TileId[][] tabOfTileId(double minX, double minY, double maxX, double maxY, int zoom){
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

    //pour tester la classe centerOn copier ce code par exemple à la fin de redraw()
    /*
    double longitude = 7.0349930;
    double latitude = 46.4969666;
    centerOn(new GeoPos((int) Units.convert(longitude, Units.Angle.DEGREE, Units.Angle.T32), (int) Units.convert(latitude, Units.Angle.DEGREE, Units.Angle.T32)));
    */
    public void centerOn(GeoPos newCenter){
        double centreX = WebMercator.x(mapParameters.getZoom(), newCenter.longitude());
        double centreY = WebMercator.y(mapParameters.getZoom(), newCenter.latitude());
        mapParameters = new MapParameters(mapParameters.getZoom(), centreX - canvas.getWidth() / 2.0, centreY - canvas.getHeight() / 2.0);
        redrawOnNextPulse();
    }
}
