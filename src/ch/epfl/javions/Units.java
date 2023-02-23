package ch.epfl.javions;

/**
 * Classe modélisant certaines unités du système SI utiles au projet
 * @author Erbland Julien (SCIPER : 346893)
 */

public final class Units {
    private Units(){}

    /**
     * Préfixes utiles pour calculer les unités en fonction de l'unité de base
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public static final double CENTI=10e-2;
    public static final double KILO=10e3;


    /**
     * Convertit la valeur donnée, exprimée dans l'unité fromUnit, en l'unité toUnit
     * @param value : Valeur à convertir
     * @param fromUnit : Unité de départ de la conversion
     * @param toUnit : Unité dans laquelle on veut convertir la valeur
     * @return : la valeur exprimée dans l'unité toUnit
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public static double convert(double value,double fromUnit ,double toUnit){
        return value*(fromUnit/toUnit);
    }


    /**
     * Equivalente à convert lorsque l'unité d'arrivée (toUnit) est l'unité de base et vaut donc 1
     * @param value : valeur à convertir exprimée dans l'unité fromUnit
     * @param fromUnit : unité en laquelle est exprimée value
     * @return : value exprimée en l'unité de référence (exemple : mètre)
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public static double convertFrom(double value, double fromUnit){
        return value*fromUnit;
    }

    /**
     * Equivalente à convert lorsque l'unité de départ (fromUnit) est l'unité de base et vaut donc 1
     * @param value : valeur à convertir exprimée dans l'unité de base
     * @param toUnit : unité en laquelle on veut convertir value
     * @return : value exprimée en l'unité toUnit
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public static double convertTo(double value, double toUnit){
        return value*(1/toUnit);
    }


    /**
     * Modélise les unités de longueurs avec comme référence le mètre
     *
     * @author Erbland Julien (SCIPER : 346893)
     */

    public static class Length{

        private Length(){}
        public static final int METER = 1;
        public static final double CENTIMETER = CENTI*METER;
        public static final double KILOMETER=KILO*METER;
        public static final double INCH=2.54*CENTIMETER;
        public static final double FOOT=12*INCH;
        public static final double NAUTICAL_MILE=1852*METER;

    }

    /**
     * Modélise les unités de temps avec comme référence la seconde
     *
     * @author Erbland Julien (SCIPER : 346893)
     */
    public static class Time{
        private Time(){}

        public static final int SECOND=1;

        public static final int MINUTE=60*SECOND;
        public static final int HOUR=60*MINUTE;

    }

    /**
     * Modélise les unités d'angles avec comme référence le radian
     *
     * @author Erbland Julien (SCIPER : 346893)
     */

    public static class Angle{
        private Angle(){}
        public static final int RADIAN =1;
        public static final double TURN=2*Math.PI*RADIAN;

        public static final double DEGREE=TURN/360;
        public static final double T32=TURN/Math.scalb(1,32);

    }

    /**
     * Modélise les unités de vitesse en utilisant le rapport en distance et temps correspondant à chaque unité
     *
     * @author Erbland Julien (SCIPER : 346893)
     */

    public static class Speed{
        private Speed(){}

        public static final double KNOT=Length.NAUTICAL_MILE/Time.HOUR;

        public static final double KILOMETER_PER_HOUR=Length.KILOMETER/Time.HOUR;

    }

}
