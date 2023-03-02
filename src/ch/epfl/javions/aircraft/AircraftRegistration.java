package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Représente le numéro d'immatriculation d'un aéronef
 * @param string : numéro d'immatriculation
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public record AircraftRegistration(String string) {

    private final static Pattern aircraftRegistrationExpression=Pattern.compile("[A-Z0-9 .?/_+-]+");

    public AircraftRegistration {
        Preconditions.checkArgument(!string.isEmpty());
        Preconditions.checkArgument(aircraftRegistrationExpression.matcher(string).matches());
    }
}
