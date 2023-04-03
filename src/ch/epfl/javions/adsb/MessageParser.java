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

    //---------- Attributs privées ----------

    private static final int VELOCITY_MESSAGE_TYPECODE = 19;
    private static final int[] POSITION_MESSAGE_LOWER_BOUND_TYPECODE = new int[] {9, 20};
    private static final int[] POSITION_MESSAGE_UPPER_BOUND_TYPECODE = new int[] {18, 22};
    private static final int[] IDENTIFICATION_MESSAGE_TYPECODES = new int[] {1, 2, 3, 4};


    //---------- Méthodes publiques ----------

    /**
     * Extrait d'un message ADS-B brut un message d'un des trois types suivants : identification, position en vol, vitesse en vol
     *
     * @param rawMessage : message ADS-B brut contenant les informations souhaitées
     * @return l'instance de AircraftIdentificationMessage, de AirbornePositionMessage ou de AirborneVelocityMessage correspondant au message brut donné
     *         ou bien null si : le code de type de ce dernier ne correspond à aucun de ces trois types de messages ou si il est invalide.
     */
    public static Message parse(RawMessage rawMessage) {
        if (rawMessage.typeCode() == IDENTIFICATION_MESSAGE_TYPECODES[0]
                || rawMessage.typeCode() == IDENTIFICATION_MESSAGE_TYPECODES[1]
                || rawMessage.typeCode() == IDENTIFICATION_MESSAGE_TYPECODES[2]
                || rawMessage.typeCode() == IDENTIFICATION_MESSAGE_TYPECODES[3]) {

            return AircraftIdentificationMessage.of(rawMessage);

        }else
            if((rawMessage.typeCode() >= POSITION_MESSAGE_LOWER_BOUND_TYPECODE[0]
                    && rawMessage.typeCode() <= POSITION_MESSAGE_UPPER_BOUND_TYPECODE[0])

                    || (rawMessage.typeCode() >= POSITION_MESSAGE_LOWER_BOUND_TYPECODE[1]
                    && rawMessage.typeCode() <= POSITION_MESSAGE_UPPER_BOUND_TYPECODE[1])) {

            return AirbornePositionMessage.of(rawMessage);

        }else if(rawMessage.typeCode() == VELOCITY_MESSAGE_TYPECODE){

            return AirborneVelocityMessage.of(rawMessage);

        }else {
            return null;
        }
    }

}
