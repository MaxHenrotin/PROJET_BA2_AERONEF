package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record CallSign(String callSign) {
    private static Pattern callSignExpression=Pattern.compile("[A-Z0-9 ]{0,8}");

    public CallSign{
        Preconditions.checkArgument(callSignExpression.matcher(callSign).matches());
    }
}
