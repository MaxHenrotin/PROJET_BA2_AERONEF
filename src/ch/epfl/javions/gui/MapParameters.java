package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.WebMercator;
import javafx.beans.property.*;

/**
 * Classe publique finale représentant les paramètres de la portion de la carte visible dans l'interface graphique
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class MapParameters {

    //===================================== Attributs privées statiques ================================================

    private static final int MIN_ZOOM = 6;
    private static final int MAX_ZOOM = 19;

    //===================================== Attributs privées ==========================================================

    private final IntegerProperty zoom;
    private final DoubleProperty minX;
    private final DoubleProperty minY;

    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur
     * @param zoom : niveau de zoom
     * @param minX : coordonnée x du coin en haut à gauche de la partie visible
     * @param minY : coordonnée y du coin en haut à gauche de la partie visible
     */
    public MapParameters(int zoom,double minX, double minY){
        Preconditions.checkArgument(Math2.clamp(MIN_ZOOM,zoom, MAX_ZOOM) == zoom);

        this.zoom = new SimpleIntegerProperty(zoom);
        this.minX = new SimpleDoubleProperty(WebMercator.x(zoom,minX));
        this.minY = new SimpleDoubleProperty(WebMercator.y(zoom,minY));
    }

    /**
     * Permet de mettre un nouveau niveau de zoom
     * @param deltaZoom : différence de zoom que l'on veut appliqué au zoom actuel
     */
    public void changeZoomLevel(int deltaZoom){
        zoom.set(zoom.get() + deltaZoom);
        Preconditions.checkArgument(Math2.clamp(MIN_ZOOM,zoom.get(), MAX_ZOOM) == zoom.get());

        minX.set(WebMercator.x(zoom.get(),minX.get()));
        minY.set(WebMercator.y(zoom.get(),minY.get()));
    }

    /**
     * Met à jour les coordonnées du coin en haut gauche x et y
     * @param x : nouvelle coordonnée x
     * @param y : nouvelle coordonnée y
     */
    public void scroll(double x, double y){
        minX.set(minX.get() + x);
        minY.set(minY.get() + y);
    }

    public ReadOnlyDoubleProperty minXProperty(){
        return minX;
    }

    public ReadOnlyDoubleProperty minYProperty(){
        return minY;
    }

    public ReadOnlyIntegerProperty zoomProperty(){
        return zoom;
    }

    public double getminX(){
        return minX.get();
    }

    public double getminY(){
        return minY.get();
    }

    public int getZoom(){
        return zoom.get();
    }


}
