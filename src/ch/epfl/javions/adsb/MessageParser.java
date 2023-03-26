package ch.epfl.javions.adsb;
//  Author:    Max Henrotin

/**
 * Classe non instanciable ayant pour but de transformer les messages ADS-B bruts en messages d'un des trois types suivants : identification, position en vol, vitesse en vol
 */
public final class MessageParser {

    private MessageParser() {} //constructeur privé pour rendre la classe non instanciable

    /**
     * Extrait d'un message ADS-B brut un message d'un des trois types suivants : identification, position en vol, vitesse en vol
     *
     * @param rawMessage : message ADS-B brut contenant les informations souhaitées
     * @return l'instance de AircraftIdentificationMessage, de AirbornePositionMessage ou de AirborneVelocityMessage correspondant au message brut donné
     *         ou bien null si : le code de type de ce dernier ne correspond à aucun de ces trois types de messages ou si il est invalide.
     */
    public static Message parse(RawMessage rawMessage){

        return null;
    }

}
