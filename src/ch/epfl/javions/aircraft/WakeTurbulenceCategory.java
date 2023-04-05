package ch.epfl.javions.aircraft;

/**
 * Représente la catégorie de turbulence de sillage d'un aéronef, il contient quatre valeurs :
 * light, medium, heavy, unknown
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM, HEAVY, UNKNOWN;

    /**
     * Retourne la catégorie de turbulence de sillage correspondant à la chaîne donnée
     * @param s : catégorie donnée en String
     * @return le type de turbulence correspondant à la chaîne passée en argument
     */
    public static WakeTurbulenceCategory of(String s){
        return switch (s) {
            case "L" -> WakeTurbulenceCategory.LIGHT;
            case "M" -> WakeTurbulenceCategory.MEDIUM;
            case "H" -> WakeTurbulenceCategory.HEAVY;
            default -> WakeTurbulenceCategory.UNKNOWN;
        };
    }
}
