package ch.epfl.javions.aircraft;

import java.util.Objects;

/**
 * Collecte les données fixes d'un aéronef
 * @param registration : numéro d'immatriculation
 * @param typeDesignator :indicateur de type
 * @param model : modèle (model), donnant généralement le nom du fabricant et celui du modèle spécifique de l'aéronef
 * @param description : description(description), un code de trois lettres donnant le type de l'aéronef, son nombre de moteurs et son type de propulsion
 * @param wakeTurbulenceCategory : catégorie de turbulence de sillage (wake turbulence category, souvent abrégée WTC), qui donne une indication de l'importance des turbulences produites dans le sillage de l'aéronef
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model, AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {

    /**
     * Constructeur compact
     * @param registration : numéro d'immatriculation
     * @param typeDesignator :indicateur de type
     * @param model : modèle (model), donnant généralement le nom du fabricant et celui du modèle spécifique de l'aéronef
     * @param description : description(description), un code de trois lettres donnant le type de l'aéronef, son nombre de moteurs et son type de propulsion
     * @param wakeTurbulenceCategory : catégorie de turbulence de sillage (wake turbulence category, souvent abrégée WTC), qui donne une indication de l'importance des turbulences produites dans le sillage de l'aéronef
     * @throws IllegalArgumentException : si une des données est null
     */
    public AircraftData{
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }
}
