package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * Interface ayant pour but d'être implémentée par toutes les classes représentant des messages ADS-B «analysés»
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public interface Message {

    /**
     * Retourne l'horodatage du message, en nanosecondes
     * @return l'horodatage du message, en nanosecondes
     */
    long timeStampNs();

    /**
     * Retourne l'adresse OACI de l'expéditeur du message.
     * @return  l'adresse OACI de l'expéditeur du message.
     */
    IcaoAddress icaoAddress();
}
