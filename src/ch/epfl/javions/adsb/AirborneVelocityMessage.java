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

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading)
                                        implements Message {

    //===================================== Attributs privées statiques ===============================================

    //---------- Constantes utiles à l'extraction des informations necessaires dans l'attribut ME ----------
    private static final int SOUS_TYPE_INDEX = 48;
    private static final int SOUS_TYPE_LENGHT = 3;
    private static final int DATA_INDEX = 21;
    private static final int DATA_LENGHT = 22;

    //------ Constantes utiles à l'extraction des informations necessaires entre les bit 21 à 42 de l'attribut ME ------

    //pour les sous-types 1 et 2
    private static final int VNS_INDEX = 0;
    private static final int DNS_INDEX = 10;
    private static final int VEW_INDEX = 11;
    private static final int DEW_INDEX = 21;
    private static final int VNS_VEW_LENGHT = 10;
    private static final int DNS_DEW_LENGHT = 1;

    //pour les sous-types 3 et 4
    //AS
    private static final int AIR_SPEED_INDEX = 0;
    private static final int AIR_SPEED_LENGHT = 10;

    //HDG
    private static final int HEADING_INDEX = 11;
    private static final int HEADING_LENGHT = 10;

    //SH
    private static final int STATUS_HEADIN_INDEX = 21;
    private static final int STATUS_HEADING_LENGHT = 1;


    //===================================== Méthodes privées statiques =================================================

    private static double calculVitesseST1_2(int sousType, double vns, double vew){
        Preconditions.checkArgument(sousType == 1 || sousType == 2);

        //norme des deux composantes de vitesse (-1 car la convention d'encodage ajoute 1 (si non-null))
        double speed = Math.hypot(vns - 1, vew - 1);

        if(sousType == 2) speed = speed*4d;

        return Units.convertFrom(speed, Units.Speed.KNOT);   //conversion de noeuds en m/s
    }

    private static double calculVitesseST3_4(int sousType, double airSpeed){
        Preconditions.checkArgument(sousType == 3 || sousType == 4);

        airSpeed -= 1; //-1 car la convention d'encodage ajoute 1 (si non-null)

        if(sousType == 4) airSpeed = airSpeed * 4d;

        return Units.convertFrom(airSpeed,Units.Speed.KNOT); //conversion de noeuds en m/s
    }

    private static double calculDirectionST1_2(double vns, double vew, double dns, double dew){
        //pour faire coincider la direction est avec l'axe x
        dew = (dew ==0) ? 1 : -1;

        //pour faire coincider la direction nord avec l'axe y
        dns = (dns == 0) ? 1 : -1;

        double speedX = dew  * (vew - 1);    //-1 car la convention d'encodage ajoute 1 (si non-null)
        double speedY = dns * (vns - 1);     //-1 car la convention d'encodage ajoute 1 (si non-null)

        double angle = Math.atan2(speedX, speedY);  //angle en radian autours de l'axe y ]-pi,pi]
        if(angle < 0) angle = angle + 2*Math.PI;   //angle en radian autours de l'axe y [0,2pi]

        return angle;
    }

    private static double calculDirectionST3_4(double heading){
        //angle en tours depuis l'axe y dans le sens horaire [0,1] (calcul selon la convention)
        double angle = Math.scalb(heading,-10);

        return Units.convertFrom(angle, Units.Angle.TURN);   //conversion de tours en radian
    }


    //===================================== Méthodes publiques statiques ===============================================

    /**
     * Permet de construire un message de vitesse en vol à partir d'un message brut
     *
     * @param rawMessage : le message brut à analyser
     * @return le message de vitesse en vol correspondant au message brut donné, ou null si le sous-type est invalide,
     *          ou si la vitesse ou la direction de déplacement ne peuvent pas être déterminés
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage){
        long attributME = rawMessage.payload();

        int sousType = Bits.extractUInt(attributME, SOUS_TYPE_INDEX, SOUS_TYPE_LENGHT);
        double content = Bits.extractUInt(attributME, DATA_INDEX, DATA_LENGHT);

        if(sousType == 1 || sousType == 2){
            double dew = Bits.extractUInt((long) content, DEW_INDEX, DNS_DEW_LENGHT);
            double vew = Bits.extractUInt((long) content, VEW_INDEX, VNS_VEW_LENGHT);
            double dns = Bits.extractUInt((long) content, DNS_INDEX, DNS_DEW_LENGHT);
            double vns = Bits.extractUInt((long) content, VNS_INDEX, VNS_VEW_LENGHT);

            if(vns == 0 || vew == 0) return null;
            return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                    calculVitesseST1_2(sousType,vns,vew), calculDirectionST1_2(vns, vew, dns, dew));

        }else if(sousType == 3 || sousType == 4){
            int statusHeading = Bits.extractUInt((long) content, STATUS_HEADIN_INDEX, STATUS_HEADING_LENGHT);
            int heading = Bits.extractUInt((long) content, HEADING_INDEX, HEADING_LENGHT);
            double airSpeed = Bits.extractUInt((long) content, AIR_SPEED_INDEX, AIR_SPEED_LENGHT);

            if(statusHeading == 0 || airSpeed == 0) return null;
            return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                    calculVitesseST3_4(sousType, airSpeed), calculDirectionST3_4(heading));

        }else{
            return null;
        }
    }


    //===================================== Méthodes publiques =========================================================

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

}
