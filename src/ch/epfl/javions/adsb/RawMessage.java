package ch.epfl.javions.adsb;

import ch.epfl.javions.*;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;

/**
 * Représente un message ADS-B «brut», c.-à-d. dont l'attribut ME n'a pas encore été analysé
 *
 * @param timeStampNs : l'horodatage du message,exprimé en nanosecondes depuis une origine donnée
 *                    (généralement l'instant correspondant au tout premier échantillon de puissance calculé)
 * @param bytes : les octets du message ADS-B (dans l'ordre de transmission)
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {

    //===================================== Attributs privés statiques =================================================

    /*
     * les constantes suivantes sont utilisées pour extraire les différents champs du message
     * selon la procédure décrite dans l'énoncé du projet
     */
    private static final int DF_VALUE = 17;
    private static final int DF_FORMAT_LENGHT = 5;
    private static final int DF_BIT_POSITION_INDEX = 3;
    private static final int FORMAT_BIT_INDEX = 0;
    private static final int ICAO_BYTE_POSITION_INDEX = 1;
    private static final int ICAO_HEX_SIZE = 6;
    private static final int ME_BYTE_POSITION_INDEX = 4;
    private static final int CRC_BYTE_POSITION_INDEX = 11;
    private static final int ME_LENGHT = 56;
    private static final int TYPECODE_LENGTH = 5;
    private static final int TYPECODE_INDEX = ME_LENGHT - TYPECODE_LENGTH;


    //===================================== Attributs publiques statiques ==============================================
    /*
     * attribut public, statique et final de la classe représentant la longueur EN OCTET des messages ADS-B.
     */
    public static final int LENGTH = 14;

    /**
     * Méthode statique retournant le message ADS-B brut avec l'horodatage et les octets donnés,
     * ou null si le CRC24 des octets ne vaut pas 0
     *
     * @param timeStampNs : l'horodatage du message,exprimé en nanosecondes depuis une origine donnée
     *                    (généralement l'instant correspondant au tout premier échantillon de puissance calculé)
     * @param bytes : les octets du message ADS-B (dans l'ordre de transmission)
     * @return le message ADS-B brut avec l'horodatage et les octets donnés,
     *          ou null si le CRC24 des octets ne vaut pas 0
     */
    public static RawMessage of(long timeStampNs, byte[] bytes){
        Crc24 crc24 = new Crc24(Crc24.GENERATOR);
        return (0 == crc24.crc(bytes)) ? new RawMessage(timeStampNs,new ByteString(bytes)) : null ;
    }

    /**
     * Méthode statique retournant la taille d'un message dont le premier octet est celui donné
     * (concrètement, retourne LENGTH si l'attribut DF contenu dans ce premier octet vaut 17,
     * et 0 pour indiquer que le message est d'un type inconnu sinon)
     *
     * @param byte0 : premier octet du message analysé
     * @return LENGTH si l'attribut DF contenu dans ce premier octet vaut 17,
     *          et 0 pour indiquer que le message est d'un type inconnu sinon
     */
    public static int size(byte byte0){
        return (Bits.extractUInt(byte0, DF_BIT_POSITION_INDEX, DF_FORMAT_LENGHT) == DF_VALUE) ? LENGTH : 0;
    }

    /**
     * Méthode statique retournant le code de type de l'attribut ME passé en argument
     *
     * @param payload : l'attribut ME analysé
     * @return le code de type de l'attribut ME passé en argument
     */
    public static int typeCode(long payload){
        return  Bits.extractUInt(payload, TYPECODE_INDEX , TYPECODE_LENGTH);
    }

    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur compact de la classe RawMessage
     *
     * @param timeStampNs : l'horodatage du message, exprimé en nanosecondes depuis une origine donnée
     *                    (généralement l'instant correspondant au tout premier échantillon de puissance calculé)
     * @param bytes : les octets du message ADS-B (dans l'ordre de transmission)
     * @throws IllegalArgumentException si l'horodatage est (strictement) négatif,
     *                                  ou si la chaîne d'octets ne contient pas LENGTH octets
     */

    public RawMessage{
        Preconditions.checkArgument(timeStampNs>=0 && bytes.size()==LENGTH);
    }

    /**
     * Méthode publique retournant le format du message ADS-B (c.-à-d. l'attribut DF stocké dans son premier octet)
     *
     * @return l'attribut DF stocké dans le premier octet du message ADS-B
     */
    public int downLinkFormat(){
        return Bits.extractUInt(bytes.byteAt(FORMAT_BIT_INDEX), DF_BIT_POSITION_INDEX, DF_FORMAT_LENGHT);
    }

    /**
     * Méthode publique retournant l'adresse OACI de l'expéditeur du message ADS-B
     *
     * @return l'adresse OACI de l'expéditeur du message
     */
    public IcaoAddress icaoAddress(){
        HexFormat hexFormat = HexFormat.of().withUpperCase();
        return new IcaoAddress( hexFormat
                                .toHexDigits
                                        (bytes.bytesInRange(ICAO_BYTE_POSITION_INDEX, ME_BYTE_POSITION_INDEX),
                                                                ICAO_HEX_SIZE));
    }

    /**
     * Méthode publique retournant l'attribut ME du message (sa «charge utile»)
     *
     * @return l'attribut ME du message ADS-B
     */
    public long payload(){
        return bytes.bytesInRange(ME_BYTE_POSITION_INDEX, CRC_BYTE_POSITION_INDEX);
    }

    /**
     * Méthode publique retournant le code de type du message
     * (c.-à-d. les cinq bits de poids le plus fort de son attribut ME)
     *
     * @return le code de type du message (les cinq bits de poids le plus fort de son attribut ME)
     */
    public int typeCode(){
        return typeCode(payload());
    }

}
