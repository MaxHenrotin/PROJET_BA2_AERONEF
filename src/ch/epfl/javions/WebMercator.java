package ch.epfl.javions;

/**
 * permet de projeter des coordonnées géographiques selon la projection WebMercator
 *
 * @author Max Henrotin (341463)
 * @author Julien Erbland (346893)
 */
public class WebMercator {
    private WebMercator(){} //constructeur privé pour rendre la classe non instantiable

    /**
     * retourne la coordonnée x correspondant à la longitude donnée (en radians) au niveau de zoom donné sur WebMercator
     * @param zoomLevel niveau de zoom (sur la projection WebMercator)
     * @param longitude longitude que l'on cherche à projeter
     * @return la coordonnée x correspondant à la longitude donnée (en radians) au niveau de zoom donné
     */
    public static double x(int zoomLevel, double longitude){
        //formule donnée en 2.5 (voir 3.8 pour + d'infos)
        return Math.scalb( Units.convertTo(longitude,Units.Angle.TURN) + 0.5 , 8+zoomLevel);
    }

    /**
     * retourne la coordonnée y correspondant à la latitude donnée (en radians) au niveau de zoom donné sur WebMercator
     * @param zoomLevel niveau de zoom (sur la projection WebMercator)
     * @param latitude latitude que l'on cherche à projeter
     * @return la coordonnée y correspondant à la latitude donnée (en radians) au niveau de zoom donné.
     */
    public static double y(int zoomLevel, double latitude){
        //formule donnée en 2.5 (voir 3.8 pour + d'infos)
        return Math.scalb( - Units.convertTo(Math2.asinh(Math.tan(latitude)),Units.Angle.TURN) + 0.5 , 8+zoomLevel);
    }
}
