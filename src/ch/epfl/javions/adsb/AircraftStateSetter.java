package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * Interface ayant pour but d'être implémentée par toutes les classes représentant l'état (modifiable) d'un aéronef.
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public interface AircraftStateSetter {

    /**
     * Change l'horodatage du dernier message reçu de l'aéronef à la valeur donnée.
     * @param timeStampNs : valeur du nouvel l'horodatage souhaité
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Change la catégorie de l'aéronef à la valeur donnée
     * @param category : valeur de la nouvelle catégorie souhaitée
     */
    void setCategory(int category);

    /**
     * Change l'indicatif de l'aéronef à la valeur donnée
     * @param callSign : valeur du nouvel indicatif souhaité
     */
    void setCallSign(CallSign callSign);

    /**
     * Change la position de l'aéronef à la valeur donnée
     * @param position : valeur de la nouvelle position souhaitée
     */
    void setPosition(GeoPos position);

    /**
     * Change l'altitude de l'aéronef à la valeur donnée
     * @param altitude : valeur de la nouvelle altitude souhaitée
     */
    void setAltitude(double altitude);

    /**
     * Change la vitesse de l'aéronef à la valeur donnée
     * @param velocity : valeur de la nouvelle vitesse souhaitée
     */
    void setVelocity(double velocity);

    /**
     * Change la direction de l'aéronef à la valeur donnée
     * @param trackOrHeading : valeur de la nouvelle direction souhaitée
     */
    void setTrackOrHeading(double trackOrHeading);
}
