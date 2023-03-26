package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

import java.util.Arrays;

import static java.lang.Math.*;

/**
 * Classe permettant de décoder la latitude et la longitude d'une aéronef
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public class CprDecoder {
    private CprDecoder(){} //classe non instanciable

    private final static double EPSILON = 1e-3;
    private final static double[] NOMBRE_LATITUDES = {60., 59.};
    private final static double[] DELTA_LATITUDES = {1./NOMBRE_LATITUDES[0] , 1./NOMBRE_LATITUDES[1]};

    private static double[] DELTA_LONGITUDES = new double[2];

    private final static double[] nombreLongitude = new double[2];

    private final static double[] latitudes = new double[2]; //exprimées en Turn puis return en T32

    private static double[] longitudes = new double[2]; //exprimées en Turn puis return en T32


    /**
     * Retourne la positon géographique correspondant aux positions locales normalisées données
     * @param x0 : longitude locale normalisée d'un message pair
     * @param y0 : latitude locale normalisée d'un message pair
     * @param x1 : longitude locale normalisée d'un message impair
     * @param y1 : latitude locale normalisée d'un message impair
     * @param mostRecent  : indique si le message reçu le plus récent est pair ou impair
     * @return la position de l'aéronef ou null si la latitude de la position décodée n'est pas valide
     * @throws IllegalArgumentException : si mostRecent ne vaut pas 0 ou 1
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent){
        Preconditions.checkArgument(mostRecent==0 || mostRecent==1);

        double[] yLatitudes = {y0, y1};

        //détermine les latitudes et retourne un boolean si les latitudes sont cohérentes
        if(calculLatitudes(yLatitudes)) {
            double[] xLatitudes = {x0, x1};

            //utilise la latitude la plus récente pour calculer le nombre de zones de longitudes
            // et vérifie si on est pas en zone polaire
            if (calculNombresLongitude(latitudes[mostRecent])) {

                calculLongitudes(xLatitudes); //détermine les longitudes

            }else {
                longitudes = xLatitudes;//cas limite où l'on est en zone polaire
            }

            for (int i = 0; i < latitudes.length; i++) { //recentre les valeurs autour de 0 et les convertie en T32
                latitudes[i] = conversionTurn(latitudes[i]);
                longitudes[i] = conversionTurn(longitudes[i]);
            }



            //retourne les coordonnées les plus récentes
            return new GeoPos((int) rint(longitudes[mostRecent]), (int) rint(latitudes[mostRecent]));

        }else {
            return null; //les coordonnées sont erronées car l'avion a changé de bande latitude
        }
    }

    private static boolean calculLatitudes(double [] yLatitudes){
        double[] indexLatitudes = new double[2];

        double z_latitude = rint(yLatitudes[0] * NOMBRE_LATITUDES[1] - yLatitudes[1] * NOMBRE_LATITUDES[0]);

        for (int i = 0; i < indexLatitudes.length; i++) {
            indexLatitudes[i] = (z_latitude<0) ? (z_latitude + NOMBRE_LATITUDES[i]) : z_latitude;

            latitudes[i] = DELTA_LATITUDES[i] * (indexLatitudes[i] + yLatitudes[i]);
        }

        return  (abs(latitudes[0]-latitudes[1]) < EPSILON);
    }

    private static void calculLongitudes(double[] xLatitudes){
        double[] indexLongitudes = new double[2];

        double z_longitude = rint(xLatitudes[0] * nombreLongitude[1] - xLatitudes[1] * nombreLongitude[0]);

        for (int i = 0; i < indexLongitudes.length; i++) {
            indexLongitudes[i] = (z_longitude < 0) ? (z_longitude + nombreLongitude[i]) : z_longitude;

            longitudes[i] = DELTA_LONGITUDES[i] * (indexLongitudes[i] + xLatitudes[i]);
        }
    }

    private static boolean calculNombresLongitude(double latitude){

        double latitudeDegree = Units.convert(latitude, Units.Angle.TURN, Units.Angle.DEGREE); //la formule ci-dessous s'utilise avec des degrées
        double A = acos(1 - ((1 - cos(2 * PI * DELTA_LATITUDES[0])) / (cos(latitudeDegree)*cos(latitudeDegree))));

        if(Double.isNaN(A)){  //en zone polaire
            nombreLongitude[0] = 1;
            nombreLongitude[1] = 0;
            return false;
        }else {
            nombreLongitude[0] = floor((2 * PI)/A);
            nombreLongitude[1] = nombreLongitude[0] - 1;
            DELTA_LONGITUDES = new double[]{1 / nombreLongitude[0], 1 / nombreLongitude[1]};
            return true;
        }
    }

    private static double conversionTurn(double angle) {
        if(angle >= 0.5){   //demi tour
            return Units.convert(angle - 1,Units.Angle.TURN,Units.Angle.T32);
        }else {
            return Units.convert(angle, Units.Angle.TURN, Units.Angle.T32);
        }
    }
}
