package ch.epfl.javions;
//  Author:    Max Henrotin

public class WebMercator {
    private WebMercator(){}
    public static double x(int zoomLevel, double longitude){
        //formule donnée en 2.5 (voir 3.8 pour + d'infos)
        return Math.scalb( Units.convertTo(longitude,Units.Angle.TURN) + 0.5 , 8+zoomLevel);
    }
    public static double y(int zoomLevel, double latitude){
        //formule donnée en 2.5 (voir 3.8 pour + d'infos)
        return Math.scalb( - Units.convertTo(Math2.asinh(Math.tan(latitude)),Units.Angle.TURN) + 0.5 , 8+zoomLevel);
    }
}
