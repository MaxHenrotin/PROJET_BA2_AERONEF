package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Représente la description d'un aéronef, un code de trois lettres donnant le type de l'aéronef, son nombre de moteurs et son type de propulsion
 * @param string : description
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public record AircraftDescription(String string) {

    //Pattern d'écriture d'une description
    private final static Pattern aircraftDescriptionExpression = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    /**
     * Constructeur
     * @param string : description
     * @throws IllegalArgumentException : si la description ne respecte pas la syntaxe
     */
    public AircraftDescription {
        Preconditions.checkArgument(aircraftDescriptionExpression.matcher(string).matches() || string.isEmpty());
    }
}
