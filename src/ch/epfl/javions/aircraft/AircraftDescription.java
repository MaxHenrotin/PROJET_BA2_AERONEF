package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftDescription(String aircraftDescription) {

    private final static Pattern aircraftDescriptionExpression = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    public AircraftDescription {
        Preconditions.checkArgument(aircraftDescriptionExpression.matcher(aircraftDescription).matches());
    }
}
