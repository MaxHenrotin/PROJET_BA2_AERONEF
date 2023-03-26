package ch.epfl.javions.adsb;
//  Author:    Max Henrotin

/**
 * Classe non instanciable ayant pour but de transformer les messages ADS-B bruts en messages d'un des trois types suivants : identification, position en vol, vitesse en vol
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
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
    public static Message parse(RawMessage rawMessage) {
        if (rawMessage.typeCode() == 1 || rawMessage.typeCode() == 2 || rawMessage.typeCode() == 3 || rawMessage.typeCode() == 4) {
            return AircraftIdentificationMessage.of(rawMessage);
        }else if((rawMessage.typeCode() >= 9 && rawMessage.typeCode() <= 18) || (rawMessage.typeCode() >= 20 && rawMessage.typeCode() <= 22)) {
            return AirbornePositionMessage.of(rawMessage);
        }else if(rawMessage.typeCode() == 19){
            return AirborneVelocityMessage.of(rawMessage);
        }else {
            return null;
        }
    }

}
