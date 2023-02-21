package ch.epfl.javions;

public final class Units {
    private Units(){}

    public static final double CENTI=10e-2;
    public static final double KILO=10e2;

    public static class Length{
        private Length(){}
        public static final double METER = 1;
        public static final double CENTIMETER = CENTI*METER;
        public static final double KILOMETER=KILO*METER;
        public static final double INCH=METER*39.370078740157;
        public static final double FOOT=METER*3.2808398950131;
        public static final double NAUTICAL_MILE=METER*0.00053995680345572;



    }

}
