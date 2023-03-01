package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftRegistration(String aircraftRegistration) {

    private final static Pattern aircraftRegistrationExpression=Pattern.compile("[A-Z0-9 .?/_+-]+");

    public AircraftRegistration {
        Preconditions.checkArgument(!aircraftRegistration.isEmpty());
        Preconditions.checkArgument(aircraftRegistrationExpression.matcher(aircraftRegistration).matches());
    }
}
