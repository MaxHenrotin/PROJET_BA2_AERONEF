package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftTypeDesignator(String string) {
/**
 * Rprésente un indicateur de type d'aéronef
 * @param string : type de l'aéronef
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
    private final static Pattern aircraftTypeDesignatorExpression=Pattern.compile("[0-9A-Z]{2,4}");

    /**
     * Constructeur
     * @param string
     * @throws IllegalArgumentException : si l'indicateur de type de ne respecte pas la syntaxe établi
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(aircraftTypeDesignatorExpression.matcher(string).matches() || string.isEmpty());
    }
}
