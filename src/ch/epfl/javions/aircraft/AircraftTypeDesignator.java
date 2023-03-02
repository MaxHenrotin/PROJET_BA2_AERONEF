package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record AircraftTypeDesignator(String TypeDesignator) {

    private final static Pattern aircraftTypeDesignatorExpression=Pattern.compile("[0-9A-Z]{2,4}");

    public AircraftTypeDesignator {
        Preconditions.checkArgument(aircraftTypeDesignatorExpression.matcher(TypeDesignator).matches() || TypeDesignator.isEmpty());
    }
}
