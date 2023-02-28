package ch.epfl.javions.aircraft;

public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM, HEAVY, UNKNOWN;

    public static WakeTurbulenceCategory of(String s){
        switch(s){
            case "L" :
                return WakeTurbulenceCategory.LIGHT;
            case "M":
                return WakeTurbulenceCategory.MEDIUM;
            case "H" :
                return WakeTurbulenceCategory.HEAVY;
        }
        return WakeTurbulenceCategory.UNKNOWN;
    }
}
