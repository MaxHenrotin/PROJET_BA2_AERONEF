package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
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
     * @param minX : coordonnée x du coin en haut à gauche de la partie visible (dans le systeme de coordonnées WebMercator)
     * @param minY : coordonnée y du coin en haut à gauche de la partie visible (dans le systeme de coordonnées WebMercator)
     */
    public MapParameters(int zoom,double minX, double minY){
        Preconditions.checkArgument(Math2.clamp(MIN_ZOOM,zoom, MAX_ZOOM) == zoom);

        this.zoom = new SimpleIntegerProperty(zoom);
        this.minX = new SimpleDoubleProperty(minX);
        this.minY = new SimpleDoubleProperty(minY);
    }

    /**
     * Permet de mettre un nouveau niveau de zoom
     * @param deltaZoom : différence de zoom que l'on veut appliqué au zoom actuel
     */
    public void changeZoomLevel(int deltaZoom){
        if (deltaZoom == 0){
            return;
        }
        zoom.set(Math2.clamp(MIN_ZOOM,zoom.get() + deltaZoom, MAX_ZOOM));
        if(deltaZoom < 0){
            minX.set(minX.get()/(2*(-deltaZoom)));
            minY.set(minY.get()/2*(-deltaZoom));
        }else{
            minX.set(minX.get()*2*deltaZoom);
            minY.set(minY.get()*2*deltaZoom);
        }
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

    /**
     * Retourne la propriété minX
     * @return : propriété de la coordonnée x du coin en haut à gauche de la partie visible
     */
    public ReadOnlyDoubleProperty minXProperty(){
        return minX;
    }

    /**
     * Retourne la propiété minY
     * @return propriété de la coordonnée y du coin en haut à gauche de la partie visible
     */
    public ReadOnlyDoubleProperty minYProperty(){
        return minY;
    }

    /**
     * Retourne la propriété zoom
     * @return : propriété du niveau de zoom
     */
    public ReadOnlyIntegerProperty zoomProperty(){
        return zoom;
    }

    /**
     * Retourne la valeur de minX
     * @return : coordonnée x du coin en haut à gauche
     */
    public double getminX(){
        return minX.get();
    }

    /**
     * Retourne la valeur de minY
     * @return : coordonnée y du coin en haut à gauche
     */
    public double getminY(){
        return minY.get();
    }

    /**
     * Retourne la valeur de zoom
     * @return : niveau de zoom
     */
    public int getZoom(){
        return zoom.get();
    }


}
