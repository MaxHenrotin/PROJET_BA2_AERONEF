package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

public record CallSign(String string) {
/**
 * Représente ce que l'on nomme l'indicatif (call sign) d'un aéronef
 * @param callSign : indicatif de l'aéronef
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

    //Pattern d'écriture d'un indicatif
    private static Pattern callSignExpression=Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * Constructeur
     * @param string
     * @throws IllegalArgumentException : si l'indicatif ne respecte pas la syntaxe
     */
    public CallSign{
        Preconditions.checkArgument(callSignExpression.matcher(string).matches());
    }
}
