package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Enregistrement représentant un message ADS-B de positionnement en vol du type
 * décrit à la section 2.2 de l'étape 5 du projet
 *
 * @param timeStampNs : l'horodatage du message, en nanosecondes
 * @param icaoAddress : l'adresse OACI de l'expéditeur du message
 * @param altitude : l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres
 * @param parity : la parité du message (0 s'il est pair, 1 s'il est impair)
 * @param x : la longitude locale et normalisée (donc comprise entre 0 et 1)
 *         à laquelle se trouvait l'aéronef au moment de l'envoi du message
 * @param y : la latitude locale et normalisée (donc comprise entre 0 et 1)
 *         à laquelle se trouvait l'aéronef au moment de l'envoi du message
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public record AirbornePositionMessage
        (long timeStampNs, IcaoAddress icaoAddress,double altitude, int parity, double x, double y) implements Message{


    //===================================== Attributs privées statiques ================================================

    //===================================== Méthodes privées statiques =================================================

    //===================================== Attributs publiques statiques ==============================================

    //===================================== Méthodes publiques statiques ===============================================

    //===================================== Attributs privées ==========================================================

    //===================================== Méthodes privées ===========================================================

    //===================================== Attributs publiques ========================================================

    //===================================== Méthodes publiques =========================================================

    private static final int Q0_BASE_ALTITUDE = -1000; //altitude de base quand Q = 0

    private static final int Q1_BASE_ALTITUDE = -1300; //altitude de base quand Q = 1

    private static final int MASK_4BITS = 0b1111; //mask qui récupère les 4 premiers bits
    private static final int MASK_GROUPE_FAIBLE = 0b111;
    private static final int MASK_GROUPE_FORT = ~MASK_GROUPE_FAIBLE;
    private static final int INVALID_ALTITUDE_VALUE = -1;
    private static final int Q0_ALTITUDE_FACTOR = 25;
    private static final int GROUPE_FORT_SIZE = 9;
    private static final int GROUPE_FAIBLE_SIZE = 3;
    private static final int Q1_GROUPE_FAIBLE_FACTOR = 100;
    private static final int Q1_GROUPE_FORT_FACTOR = 500;
    private static final int LONGITUDE_CPR_INDEX = 0;
    private static final int LATITUDE_CPR_INDEX = 17;
    private static final int LONGITUDE_CPR_LENGTH = 17;
    private static final int LATITUDE_CPR_LENGTH = 17;
    private static final int ALTITUDE_INDEX = 36;
    private static final int ALTITUDE_LENGTH = 12;
    public static final int FORMAT_INDEX = 34;
    public static final int FORMAT_LENGTH = 1;
    private static final int Q_INDEX = 4; //position du bit Q
    private static final int Q_MASK = 1 << Q_INDEX; //mask permettant de récupérer le bit Q
    private static final int D2_INDEX = 2;
    private static final int D4_INDEX = 1;
    private static final int A1_INDEX = 10;
    private static final int A2_INDEX = 8;
    private static final int A4_INDEX = 6;
    private static final int B1_INDEX = 5;
    private static final int B2_INDEX = 3;
    private static final int B4_INDEX = 1;
    private static final int C1_INDEX = 11;
    private static final int C2_INDEX = 9;
    private static final int C4_INDEX = 7;

    private static final int NORMALISATION_EXPOSANT = -17;

    //mask de {D1, D2, D4, A1, A2, A4, B1, B2, B4, C1, C2, C4}
    //Ces masks permettent le démêlage dans le cas où le bit Q vaut 0
    private static final int[] MASK_DEMELAGE = {Q_MASK, 1 << D2_INDEX, D4_INDEX, 1 << A1_INDEX,
            1 << A2_INDEX, 1 << A4_INDEX, 1 << B1_INDEX, 1 << B2_INDEX,
            1 <<B4_INDEX, 1 << C1_INDEX, 1 << C2_INDEX, 1 << C4_INDEX};


    //---------- Méthodes privées ----------

    /**
     * Calcule l'altitude en fonction de l'attribut alt issus de l'attribut ME du message brut
     *
     * @param alt : la chaîne de bits contenant l'altitude dans l'attribut ME
     * @return l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message,
     * en mètres ou -1 si l'altitude est invalide
     */

    private static int calculAltitude(int alt){
        if((alt & Q_MASK) == Q_MASK){ //Q = 1
            int temp = MASK_4BITS & alt;
            alt = alt >> 5;
            alt = (alt << 4) | temp;

            return Q0_BASE_ALTITUDE + alt * Q0_ALTITUDE_FACTOR;

        }else{ //Q = 0
            //remet les bits dans le bon ordre
            int demele = demeleIndex(alt);


            //---Calcul du groupe fort---

            //récupère les bits du groupe fort
            int groupeFort = (demele & MASK_GROUPE_FORT) >> GROUPE_FAIBLE_SIZE;
            //réalise le décodage  de Gray
            groupeFort = decodeGray(groupeFort, GROUPE_FORT_SIZE);

            //---Calcul du groupe faible---

            //récupère les bits du groupe faible
            int groupeFaible = demele & MASK_GROUPE_FAIBLE;
            //réalise le décodage de Gray
            groupeFaible = decodeGray( groupeFaible, GROUPE_FAIBLE_SIZE);
            //réalise les dernières transformations sur le groupe faible
            boolean groupeFortOdd = (groupeFort % 2) == 1;
            groupeFaible =transformationGroupeFaible(groupeFaible, groupeFortOdd);

            //retourne l'altitude calculée en vérifiant qu'elle soit bien valide
            return (groupeFaible != INVALID_ALTITUDE_VALUE) ?
                    (Q1_BASE_ALTITUDE + groupeFaible * Q1_GROUPE_FAIBLE_FACTOR + groupeFort * Q1_GROUPE_FORT_FACTOR)
                    : INVALID_ALTITUDE_VALUE;
        }
    }

    /**
     * Réalise le démêlage des bits comme ceci :
     * C1 A1 C2 A2 C4 A4 B1 D1 B2 D2 B4 D4 -> D1 D2 D4 A1 A2 A4 B1 B2 B4 C1 C2 C4
     * MASK_DEMELAGE permet d'avoir le mask pour récupérer la valeur du bit D1 de l'input
     * puis D2 puis D4 et ainsi de suite
     *
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
     *
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
     *
     * @param groupeFaible : la valeur du groupe faible
     * @param groupeFortImpair : permet de connaitre la parité du groupe fort
     * @return la valeur trnasformée du groupe faible
     */
    private static int transformationGroupeFaible(int groupeFaible, boolean groupeFortImpair){
        if(groupeFaible == 0 || groupeFaible == 5 || groupeFaible == 6){
            return INVALID_ALTITUDE_VALUE;
        } else if (groupeFaible == 7) {
            if(groupeFortImpair){
                //symétrique de 5
                return 1;
            }else{
                return 5;
            }

        }else if(groupeFortImpair){
            //on retourne la valeur symétrique centrale si le groupe fort est impaire
            return 6 - groupeFaible;
        }else {
            return groupeFaible;
        }
    }

    //---------- Méthodes publiques ----------
    /**
     * Méthode publique statique retournant le message de positionnement en vol correspondant au message brut donné,
     * ou null si l'altitude qu'il contient est invalide (voir point 2.2.5.2 de l'étape 5 du projet).
     *
     * @param rawMessage : le message brut dont on doit extraire les informations souhaitées
     * @return le message de positionnement en vol correspondant au message brut donné,
     * ou null si l'altitude qu'il contient est invalide
     */
    public static AirbornePositionMessage of(RawMessage rawMessage){
        long attributME = rawMessage.payload();

        double lon_cpr = Bits.extractUInt(attributME, LONGITUDE_CPR_INDEX, LONGITUDE_CPR_LENGTH);
        double lat_cpr = Bits.extractUInt(attributME, LATITUDE_CPR_INDEX, LATITUDE_CPR_LENGTH);
        int format = Bits.extractUInt(attributME, FORMAT_INDEX, FORMAT_LENGTH); // 0 = paire, 1 = impaire
        int alt = Bits.extractUInt(attributME, ALTITUDE_INDEX, ALTITUDE_LENGTH);

        int altitude = calculAltitude(alt);

        if(altitude != INVALID_ALTITUDE_VALUE){
            return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                    Units.convertFrom(altitude,Units.Length.FOOT), format,
                    Math.scalb(lon_cpr,NORMALISATION_EXPOSANT), Math.scalb(lat_cpr,NORMALISATION_EXPOSANT));
        }else {
            return null;
        }
    }


    //---------- Constructeur ----------

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
}
