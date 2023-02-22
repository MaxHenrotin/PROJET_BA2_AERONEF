package ch.epfl.javions;

public final class Units {
    private Units(){}

    public static final double CENTI=10e-2;
    public static final double KILO=10e3;

    public static double convert(double value,double fromUnit ,double toUnit){
        return value*(fromUnit/toUnit);
    }

    public static double convertFrom(double value, double fromUnit){
        return value*fromUnit;
    }

    public static double convertTo(double value, double toUnit){
        return value*(1/toUnit);
    }

    public static class Length{

        private Length(){}
        public static final int METER = 1;
        public static final double CENTIMETER = CENTI*METER;
        public static final double KILOMETER=KILO*METER;
        public static final double INCH=2.54*CENTIMETER;
        public static final double FOOT=12*INCH;
        public static final double NAUTICAL_MILE=1852*METER;

    }

    public static class Time{
        private Time(){}

        public static final int SECOND=1;

        public static final int MINUTE=60*SECOND;
        public static final int HOUR=60*MINUTE;

    }

    public static class Angle{
        private Angle(){}
        public static final int RADIAN =1;
        public static final double TURN=2*Math.PI*RADIAN;

        public static final double DEGREE=TURN/360;
        public static final double T32=TURN/Math.scalb(1,32);

    }

    public static class Speed{
        private Speed(){}

        public static final double KNOT=Length.NAUTICAL_MILE/Time.HOUR;

        public static final double KILOMETER_PER_HOUR=Length.KILOMETER/Time.HOUR;

    }

}
