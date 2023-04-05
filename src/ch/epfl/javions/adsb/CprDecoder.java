package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

import static java.lang.Math.*;

/**
 * Classe permettant de décoder la latitude et la longitude d'une aéronef
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public class CprDecoder {


    //===================================== Attributs privées statiques ================================================

    private static final int ODD_INDEX = 1;
    private static final int EVEN_INDEX = 0;
    private static final int ONE_TURN = 1;
    private static final double UPPER_BOUND_LATITUDE_LIMIT = 0.75d;
    private static final double LOWER_BOUND_LATITUDE_LIMIT = 0.25d;
    private static final  double[] NOMBRE_LATITUDES = {60d, 59d};
    private static final  double[] DELTA_LATITUDES = {1d / NOMBRE_LATITUDES[EVEN_INDEX],
                                                        1d / NOMBRE_LATITUDES[ODD_INDEX]};
    private static final  double[] nombreLongitude = new double[2];
    private static final  double[] latitudes = new double[2]; //exprimées en Turn puis return en T32
    private static double[] longitudes = new double[2]; //exprimées en Turn puis return en T32
    private static double[] DELTA_LONGITUDES = new double[2];
    private static boolean inPolarZone;


    //===================================== Méthodes privées statiques =================================================
    private static void calculLatitudes(double [] yLatitudes){
        double[] indexLatitudes = new double[2];

        double z_latitude = rint(yLatitudes[EVEN_INDEX] * NOMBRE_LATITUDES[ODD_INDEX]
                                - yLatitudes[ODD_INDEX] * NOMBRE_LATITUDES[EVEN_INDEX]);

        for (int i = 0; i < indexLatitudes.length; i++) {
            indexLatitudes[i] = (z_latitude < 0) ? (z_latitude + NOMBRE_LATITUDES[i]) : z_latitude;

            latitudes[i] = DELTA_LATITUDES[i] * (indexLatitudes[i] + yLatitudes[i]);

        }
    }

    private static void calculLongitudes(double[] xLatitudes){
        double[] indexLongitudes = new double[2];

        double z_longitude = rint(xLatitudes[EVEN_INDEX] * nombreLongitude[ODD_INDEX]
                                    - xLatitudes[ODD_INDEX] * nombreLongitude[EVEN_INDEX]);

        for (int i = 0; i < indexLongitudes.length; i++) {
            indexLongitudes[i] = (z_longitude < 0) ? (z_longitude + nombreLongitude[i]) : z_longitude;

            longitudes[i] = DELTA_LONGITUDES[i] * (indexLongitudes[i] + xLatitudes[i]);
        }
    }

    private static boolean calculNombresLongitude(){

        //la formule ci-dessous s'utilise avec des radians
        double latitudeRadianEven = Units.convert(latitudes[EVEN_INDEX], Units.Angle.TURN, Units.Angle.RADIAN);
        double latitudeRadianOdd = Units.convert(latitudes[ODD_INDEX], Units.Angle.TURN, Units.Angle.RADIAN);

        double AEven = acos(1 - ((1 - cos(2 * PI * DELTA_LATITUDES[EVEN_INDEX]))
                                    / (cos(latitudeRadianEven) * cos(latitudeRadianEven))));
        double AOdd = acos(1 - ((1 - cos(2 * PI * DELTA_LATITUDES[EVEN_INDEX]))
                                    / (cos(latitudeRadianOdd) * cos(latitudeRadianOdd))));

        if(Double.isNaN(AEven) && Double.isNaN(AOdd)){  //en zone polaire
            nombreLongitude[EVEN_INDEX] = 1;
            nombreLongitude[ODD_INDEX] = 0;
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
                nombreLongitude[EVEN_INDEX] = nombreLongitudesEven;
                nombreLongitude[ODD_INDEX] = nombreLongitude[EVEN_INDEX] - 1;
                DELTA_LONGITUDES = new double[]{1d / nombreLongitude[EVEN_INDEX], 1d / nombreLongitude[ODD_INDEX]};
                return true;
            }else {
                return false;
            }
        }
    }

    private static double conversionTurn(double angle) {
       return (angle >= 0.5) ? Units.convert(angle - ONE_TURN,Units.Angle.TURN,Units.Angle.T32) :
                                 Units.convert(angle, Units.Angle.TURN, Units.Angle.T32);

    }

    //===================================== Méthodes privées ===========================================================

    private CprDecoder(){} //classe non instanciable

    //===================================== Méthodes publiques statiques ===============================================

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

        double[] messageLatitudes = {y0, y1};
        double[] messageLongitudes = {x0, x1};

        //détermine les latitudes
        calculLatitudes(messageLatitudes);

        //utilise la latitude pour calculer le nombre de zones de longitudes
        // et vérifie si on est pas en zone polaire
        if (calculNombresLongitude()){

            calculLongitudes(messageLongitudes); //détermine les longitudes

        }else {
            if (inPolarZone){
                longitudes = messageLongitudes;//cas limite où l'on est en zone polaire
            }else {
                //cas limite où l'on a changé de bande de lattitude de zone polaire à non polaire ou inversement
                return null;
            }
        }

        //test si lattitude calculéee est valide
        if(latitudes[mostRecent] > LOWER_BOUND_LATITUDE_LIMIT && latitudes[mostRecent] < UPPER_BOUND_LATITUDE_LIMIT)
            return null;


        //recentre les valeurs autour de 0 et les convertie en T32
        for (int i = 0; i < latitudes.length; i++) {
            latitudes[i] = conversionTurn(latitudes[i]);
            longitudes[i] = conversionTurn(longitudes[i]);
        }

        //retourne les coordonnées les plus récentes
        return new GeoPos((int) rint(longitudes[mostRecent]), (int) rint(latitudes[mostRecent]));

    }
}
