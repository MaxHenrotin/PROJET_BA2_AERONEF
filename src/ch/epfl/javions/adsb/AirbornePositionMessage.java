package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Enregistrement représentant un message ADS-B de positionnement en vol du type décrit à la section 2.2 de l'étape 5 du projet
 *
 * @param timeStampNs : l'horodatage du message, en nanosecondes
 * @param icaoAddress : l'adresse OACI de l'expéditeur du message
 * @param altitude : l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres
 * @param parity : la parité du message (0 s'il est pair, 1 s'il est impair)
 * @param x : la longitude locale et normalisée (donc comprise entre 0 et 1) à laquelle se trouvait l'aéronef au moment de l'envoi du message
 * @param y : la latitude locale et normalisée (donc comprise entre 0 et 1) à laquelle se trouvait l'aéronef au moment de l'envoi du message
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress,double altitude, int parity, double x, double y) implements Message{

    private static final int Q0_BASE_ALTITUDE = -1000;

    private static final int Q1_BASE_ALTITUDE = -1300;
    private static final int Q_INDEX = 4;

    private static final int Q_MASK = 1 << Q_INDEX;

    private static final int MASK_4 = 0b1111;

    //mask de {D1, D2, D4, A1, A2, A4, B1, B2, B4, C1, C2, C4}
    private static final int[] MASK_DEMELAGE = {Q_MASK, 1<<2, 1, 1<<10, 1<<8, 1<<6, 1<<5, 1<<3, 1<<1, 1<<11, 1<<9, 1<<7};

    private static final int MASK_GROUPE_FAIBLE = 0b111;

    private static final int MASK_GROUPE_FORT = ~MASK_GROUPE_FAIBLE;

    /**
     * Constructeur compact
     * @param timeStampNs : l'horodatage du message, en nanosecondes
     * @param icaoAddress : l'adresse OACI de l'expéditeur du message
     * @param altitude : l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres
     * @param parity : la parité du message (0 s'il est pair, 1 s'il est impair)
     * @param x : la longitude locale et normalisée (donc comprise entre 0 et 1) à laquelle se trouvait l'aéronef au moment de l'envoi du message
     * @param y : la latitude locale et normalisée (donc comprise entre 0 et 1) à laquelle se trouvait l'aéronef au moment de l'envoi du message
     *
     * @throws NullPointerException si icaoAddress est nul
     * @throws IllegalArgumentException si timeStamp est strictement inférieure à 0, ou parity est différent de 0 ou 1, ou x ou y ne sont pas compris entre 0 (inclus) et 1 (exclu)
     */
    public AirbornePositionMessage{
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs>=0 && (parity==0 || parity==1) && (x>=0 && x<1) && (y>=0 && y<1));
    }

    /**
     * Méthode publique statique retournant le message de positionnement en vol correspondant au message brut donné, ou null si l'altitude qu'il contient est invalide (voir point 2.2.5.2 de l'étape 5 du projet).
     * @param rawMessage : le message brut dont on doit extraire les informations souhaitées
     * @return le message de positionnement en vol correspondant au message brut donné, ou null si l'altitude qu'il contient est invalide
     */
    public static AirbornePositionMessage of(RawMessage rawMessage){
        long attributME = rawMessage.payload();

        double lon_cpr = Bits.extractUInt(attributME, 0, 17);
        double lat_cpr = Bits.extractUInt(attributME, 17, 17);
        int format = Bits.extractUInt(attributME, 34, 1); // 0 = paire, 1 = impaire
        int alt = Bits.extractUInt(attributME, 36, 12);

        int altitude = calculAltitude(alt);

        if(altitude != -1){
            return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), Units.convertFrom(altitude,Units.Length.FOOT), format, Math.scalb(lon_cpr,-17), Math.scalb(lat_cpr,-17));
        }else {
            return null;
        }
    }

    /**
     * Calcule l'altitude en fonction de l'attribut alt issus de l'attribut ME du message brut
     * @param alt : la chaîne de bits contenant l'altitude dans l'attribut ME
     * @return l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètresou -1 si l'altitude est invalide
     */

    private static int calculAltitude(int alt){
        if((alt & Q_MASK) == Q_MASK){ //Q = 1
            int temp = MASK_4 & alt;
            alt = alt >> 5;
            alt = (alt << 4) | temp;

            return Q0_BASE_ALTITUDE + alt * 25;

        }else{ //Q = 0
            int demele = demeleIndex(alt);
            int groupeFort = decodeGray( (demele & MASK_GROUPE_FORT)>>3, 9);
            int groupeFaible =transformationGroupeFaible(decodeGray( demele & MASK_GROUPE_FAIBLE, 3), (groupeFort%2)==1);

            return (groupeFaible != -1) ? (Q1_BASE_ALTITUDE + groupeFaible * 100 + groupeFort * 500) : -1;
        }
    }

    /**
     * Réalise le démêlage des bits comme ceci : C1 A1 C2 A2 C4 A4 B1 D1 B2 D2 B4 D4 -> D1 D2 D4 A1 A2 A4 B1 B2 B4 C1 C2 C4
     * MASK_DEMELAGE permet d'avoir le mask pour récupérer la valeur du bit D1 de l'input puis D2 puis D4 et ainsi de suite
     * @param input : ensemble des bits à démêler
     * @return les bits démêlés
     */
    private static int demeleIndex(int input){
        int output = 0;

        for (int mask : MASK_DEMELAGE) {
            int bit = ((input & mask) == mask) ? 1 : 0;
            output = (output << 1) | bit;
        }
        return output;
    }

    /**Permet de décoder un code de Graay en interprétation binaire
     * @param input : code de Gray
     * @param size : nombre de bits du code
     * @return l'interprétation binaire du code de Gray
     */
    private static int decodeGray(int input, int size){
        int output = 0;
        for (int i = 0; i < size; i++) {
            output ^= (input >> i);
        }
        return output;
    }

    /**
     * Réalise les transformations nécessaires sur le groupe faible dans le décodage de l'altitude
     * @param groupeFaible : la valeur du groupe faible
     * @param groupeFortImpair : permet de connaitre la parité du groupe fort
     * @return la valeur trnasformée du groupe faible
     */
    private static int transformationGroupeFaible(int groupeFaible, boolean groupeFortImpair){
        if(groupeFaible == 0 || groupeFaible == 5 || groupeFaible == 6){
            return -1;
        } else if (groupeFaible == 7) {
            if(groupeFortImpair){
                return 1;
            }else{
                return 5;
            }
        }else if(groupeFortImpair){
            return 6 - groupeFaible;
        }else {
            return groupeFaible;
        }
    }

}
