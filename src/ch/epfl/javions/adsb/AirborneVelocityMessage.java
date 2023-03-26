package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Enregistrement représentant un message de vitesse en vol (du type décrit à la section 2.1 de l'étape 6 du projet)
 *
 * @param timeStampNs : l'horodatage du message, en nanosecondes
 * @param icaoAddress : l'adresse OACI de l'expéditeur du message
 * @param speed : la vitesse de l'aéronef, en m/s
 * @param trackOrHeading : la direction de déplacement de l'aéronef, en radians
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading) {

    /**
     * Constructeur compact
     * @param timeStampNs : l'horodatage du message, en nanosecondes
     * @param icaoAddress : l'adresse OACI de l'expéditeur du message
     * @param speed : la vitesse de l'aéronef, en m/s
     * @param trackOrHeading : la direction de déplacement de l'aéronef, en radians
     *
     * @throws NullPointerException si icaoAddress est nul
     * @throws IllegalArgumentException si timeStampNs, speed ou trackOrHeading sont strictement négatifs
     */
    public AirborneVelocityMessage{
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs>=0 && speed>=0 && trackOrHeading>=0);
    }

    /**
     * Permet de construire un message de vitesse en vol à partir d'un message brut
     * @param rawMessage : le message brut à analyser
     * @return le message de vitesse en vol correspondant au message brut donné, ou null si le sous-type est invalide, ou si la vitesse ou la direction de déplacement ne peuvent pas être déterminés
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage){
        long attributME = rawMessage.payload();

        double sousType = Bits.extractUInt(attributME, 48, 3);  //ST
        double content = Bits.extractUInt(attributME, 21, 22);  //-

        if(sousType == 1 || sousType == 2){
            double dew = Bits.extractUInt((long) content, 21, 1);
            double vew = Bits.extractUInt((long) content, 11, 10);
            double dns = Bits.extractUInt((long) content, 10, 1);
            double vns = Bits.extractUInt((long) content, 0, 10);
            if(vns == 0 || vew == 0){
                return null;
            }


            return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speed, direction);
        }else if(sousType == 3 || sousType == 4){
            double statusHeading = Bits.extractUInt((long) content, 21, 1); //SH
            double heading = Bits.extractUInt((long) content, 11, 10);      //HDG
            double airSpeed = Bits.extractUInt((long) content, 0, 10);      //AS

            return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speed, direction);
        }else{
            return null;
        }
    }
}
