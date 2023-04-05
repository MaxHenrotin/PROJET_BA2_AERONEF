package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Représente ce que l'on nomme l'indicatif (call sign) d'un aéronef
 *
 * @param string : indicatif de l'aéronef
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public record CallSign(String string) {

    //===================================== Attributs privées statiques ================================================

    //Pattern d'écriture d'un indicatif
    private static final Pattern callSignExpression = Pattern.compile("[A-Z0-9 ]{0,8}");

    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur compact
     *
     * @param string : indicatif de l'aéronef
     * @throws IllegalArgumentException : si l'indicatif ne respecte pas la syntaxe
     */
    public CallSign {
        Preconditions.checkArgument(callSignExpression
                                    .matcher(string)
                                    .matches());
    }
}
