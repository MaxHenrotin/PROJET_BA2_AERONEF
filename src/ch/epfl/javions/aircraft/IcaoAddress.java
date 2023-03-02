package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record IcaoAddress(String string) {

    private final static Pattern icaoAddressExpression=Pattern.compile("[0-9A-F]{6}");

    public IcaoAddress{
        Preconditions.checkArgument(!string.isEmpty());
        Preconditions.checkArgument(icaoAddressExpression.matcher(string).matches());
    }
}
