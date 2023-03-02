package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Rprésente un indicateur de type d'aéronef
 * @param TypeDesignator : type de l'aéronef
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public record AircraftTypeDesignator(String TypeDesignator) {

    private final static Pattern aircraftTypeDesignatorExpression=Pattern.compile("[0-9A-Z]{2,4}");

    /**
     * Constructeur
     * @param TypeDesignator
     * @throws IllegalArgumentException : si l'indicateur de type de ne respecte pas la syntaxe établi
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(aircraftTypeDesignatorExpression.matcher(TypeDesignator).matches() || TypeDesignator.isEmpty());
    }
}
