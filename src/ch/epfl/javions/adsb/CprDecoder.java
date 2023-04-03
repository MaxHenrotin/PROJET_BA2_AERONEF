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

    private static boolean inPolarZone;


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
        double[] xLongitudes = {x0, x1};

        //détermine les latitudes et retourne un boolean si les latitudes sont cohérentes
        calculLatitudes(yLatitudes);


        //utilise la latitude la plus récente pour calculer le nombre de zones de longitudes
        // et vérifie si on est pas en zone polaire
        if (calculNombresLongitude()){

            calculLongitudes(xLongitudes); //détermine les longitudes

        }else {
            if (inPolarZone){
                longitudes = xLongitudes;//cas limite où l'on est en zone polaire
            }else {
                return null;
            }
        }

        if(latitudes[mostRecent] > 0.25d && latitudes[mostRecent] < 0.75d){ //test si lattitude calculéee est valide
            return null;
        }

        for (int i = 0; i < latitudes.length; i++) { //recentre les valeurs autour de 0 et les convertie en T32
            latitudes[i] = conversionTurn(latitudes[i]);
            longitudes[i] = conversionTurn(longitudes[i]);
        }

        //retourne les coordonnées les plus récentes
        return new GeoPos((int) rint(longitudes[mostRecent]), (int) rint(latitudes[mostRecent]));

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
        double[] indexLongitudes = new double[2];

        double z_longitude = rint(xLatitudes[0] * nombreLongitude[1] - xLatitudes[1] * nombreLongitude[0]);

        for (int i = 0; i < indexLongitudes.length; i++) {
            indexLongitudes[i] = (z_longitude < 0) ? (z_longitude + nombreLongitude[i]) : z_longitude;

            longitudes[i] = DELTA_LONGITUDES[i] * (indexLongitudes[i] + xLatitudes[i]);
        }
    }

    private static boolean calculNombresLongitude(){

        double latitudeRadianEven = Units.convert(latitudes[0], Units.Angle.TURN, Units.Angle.RADIAN); //la formule ci-dessous s'utilise avec des radians
        double latitudeRadianOdd = Units.convert(latitudes[1], Units.Angle.TURN, Units.Angle.RADIAN);
        double AEven = acos(1 - ((1 - cos(2 * PI * DELTA_LATITUDES[0])) / (cos(latitudeRadianEven)*cos(latitudeRadianEven))));
        double AOdd = acos(1 - ((1 - cos(2 * PI * DELTA_LATITUDES[0])) / (cos(latitudeRadianOdd)*cos(latitudeRadianOdd))));

        if(Double.isNaN(AEven) && Double.isNaN(AOdd)){  //en zone polaire
            nombreLongitude[0] = 1;
            nombreLongitude[1] = 0;
            inPolarZone = true;
            return false;
        }else {
            double nombreLongitudesEven;
            inPolarZone = false;

            //cas limite où l'avion a changé de bande de latitude et est passé d'une zone polaire à une zone non polaire
            if (Double.isNaN(AEven) || Double.isNaN(AOdd)){
                return false;
            }

            if((nombreLongitudesEven = floor((2. * PI)/AEven)) == floor((2.*PI)/AOdd)) {
                nombreLongitude[0] = nombreLongitudesEven;
                nombreLongitude[1] = nombreLongitude[0] - 1;
                DELTA_LONGITUDES = new double[]{1 / nombreLongitude[0], 1 / nombreLongitude[1]};
                return true;
            }else {
                return false;
            }
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
