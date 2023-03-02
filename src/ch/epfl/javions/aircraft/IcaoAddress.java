package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Représente une adresse OACI
 * @param string : chaîne contenant la représentation textuelle de l'adresse OACI
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public record IcaoAddress(String string) {

    //Pattern d'écriture d'une adresse ICAO
    private final static Pattern icaoAddressExpression=Pattern.compile("[0-9A-F]{6}");

    /**
     * Constructeur compact
     * @param string : adresse ICAO de l'aéronef
     * @throws IllegalArgumentException: si l'adresse est vide
     * @throws IllegalArgumentException : si l'adresse ne respecte pas la syntaxe
     */
    public IcaoAddress{
        Preconditions.checkArgument(!string.isEmpty());
        Preconditions.checkArgument(icaoAddressExpression.matcher(string).matches());
    }
}
