package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
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

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading) implements Message {

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

            //vitesse
            double speed = Math.hypot(vns-1, vew-1);    //norme des deux composantes de vitesse (-1 car par convention ils sont stockés avec un décalage de 1, voir la doc etape 6 pt. 2.1.2)
            if(sousType == 2){ speed = speed*4; }
            speed = Units.convertFrom(speed, Units.Speed.KNOT);   //conversion de noeuds en m/s

            //direction
            //pour faire coincider la direction est avec l'axe x
            if(dew == 0){
                dew = 1;
            }else{
                dew = -1;
            }
            //pour faire coincider la direction nord avec l'axe y
            if(dns == 0) {
                dns = 1;
            }else{
                dns = -1;
            }
            double speedX = dew*(vew-1);    //(-1 car par convention ils sont stockés avec un décalage de 1, voir la doc etape 6 pt. 2.1.2)
            double speedY = dns*(vns-1);    //(-1 car par convention ils sont stockés avec un décalage de 1, voir la doc etape 6 pt. 2.1.2)
            double angle = Math.atan2(speedX, speedY);  //angle en radian autours de l'axe y ]-pi,pi]
            if(angle < 0){
                angle = angle + 2*Math.PI;   //angle en radian autours de l'axe y [0,2pi]
            }

            return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speed, angle);

        }else if(sousType == 3 || sousType == 4){
            int statusHeading = Bits.extractUInt((long) content, 21, 1); //SH
            int heading = Bits.extractUInt((long) content, 11, 10);      //HDG
            double airSpeed = Bits.extractUInt((long) content, 0, 10) - 1;      //AS    //-1 car c'est la convention d'encodage

            if(statusHeading == 0 || airSpeed == 0){

                return null;

            }else {
            //statusHeading == 1
                //vitesse
                if(sousType == 4){ airSpeed = airSpeed * 4d; }
                airSpeed = Units.convertFrom(airSpeed,Units.Speed.KNOT);

                //direction
                double angle = Math.scalb(heading,-10); //angle en tours depuis l'axe y dans le sens horaire [0,1] (calcul selon la convention donnée au point 2.1.3 de l'étape 6)
                angle = Units.convertFrom(angle, Units.Angle.TURN);   //conversion de tours en radian

                return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), airSpeed, angle);
            }
        }else{
            return null;
        }
    }
}
