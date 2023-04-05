package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Rprésente un indicateur de type d'aéronef
 * @param string : type de l'aéronef
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public record AircraftTypeDesignator(String string) {

    private final static Pattern aircraftTypeDesignatorExpression=Pattern.compile("[0-9A-Z]{2,4}");

    /**
     * Constructeur compact
     * @param string : indicateur de type de l'aéronef
     * @throws IllegalArgumentException : si l'indicateur de type de ne respecte pas la syntaxe établie
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(aircraftTypeDesignatorExpression
                                                .matcher(string)
                                                .matches() || string.isEmpty());
    }
}
