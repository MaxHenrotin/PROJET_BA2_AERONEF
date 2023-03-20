package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

import java.util.Arrays;

import static java.lang.Math.*;

public class CprDecoder {

    private CprDecoder(){} //classe non instanciable


    private final static double[] NOMBRE_LATITUDES = {60., 59.};
    private final static double[] DELTA_LATITUDES = {1./NOMBRE_LATITUDES[0] , 1./NOMBRE_LATITUDES[1]};

    private static double[] nombreLongitude = new double[2];

    private static double[] latitudes = new double[2];

    private static double[] longitudes = new double[2];

    private static boolean inPolarZone = false;

    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent){
        Preconditions.checkArgument(mostRecent==0 || mostRecent==1);

        calculLatitudes(new double[] {y0, y1}); //determine les latitudes

        calculNombresLongitude(Units.convert(latitudes[mostRecent],Units.Angle.TURN,Units.Angle.DEGREE)); //utilise la latitude la plus récente pour calculer le nombre de zones de longitudes

        calculLongitudes(new double[]{x0, x1}); //determine les longitudes


        for (int i = 0; i < latitudes.length; i++) {
            latitudes[i] = conversionTurn(latitudes[i]);
            longitudes[i] = conversionTurn(longitudes[i]);
        }

        return new GeoPos((int) longitudes[mostRecent],(int) latitudes[mostRecent]);
    }



    private static void calculLatitudes(double [] yLatitudes){
        double[] indexLatitudes = new double[2];

        double z_latitude = rint(yLatitudes[0] * NOMBRE_LATITUDES[1] - yLatitudes[1] * NOMBRE_LATITUDES[0]);

        for (int i = 0; i < indexLatitudes.length; i++) {
            indexLatitudes[i] = (z_latitude<0) ? (z_latitude + NOMBRE_LATITUDES[i]) : z_latitude;

            latitudes[i] = DELTA_LATITUDES[i] * (indexLatitudes[i] + yLatitudes[i]);
        }


    }

    private static void calculLongitudes(double[] xLatitudes){
        if(inPolarZone){
            longitudes = xLatitudes.clone();
        }else {
            double[] indexLongitudes = new double[2];

            double z_longitude = rint(xLatitudes[0] * nombreLongitude[1] - xLatitudes[1] * nombreLongitude[0]);

            for (int i = 0; i < indexLongitudes.length; i++) {
                indexLongitudes[i] = (z_longitude < 0) ? (z_longitude + nombreLongitude[i]) : z_longitude;

                longitudes[i] = (1/nombreLongitude[i]) * (indexLongitudes[i] + xLatitudes[i]);
            }
        }

    }

    private static void calculNombresLongitude(double longitude){
        double A = acos(1 - ((1 - cos(2 * PI * DELTA_LATITUDES[0])) / (cos(longitude)*cos(longitude))));

        if(Double.isNaN(A)){
            nombreLongitude[0] = 1;
            nombreLongitude[1] = 0;
            inPolarZone = true;
        }else {
            nombreLongitude[0] = floor((2 * PI)/A);
            nombreLongitude[1] = nombreLongitude[0] - 1;
        }
    }

    private static double conversionTurn(double angle) {
        if(angle >= 0.5){
            return Units.convert(angle - Units.Angle.TURN,Units.Angle.TURN,Units.Angle.T32);
        }else {
            return Units.convert(angle, Units.Angle.TURN, Units.Angle.T32);
        }
    }
}
