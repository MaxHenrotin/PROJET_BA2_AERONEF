package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Enregistrement représentant un message ADS-B d'identification et de catégorie, du type décrit au point 2.1 de l'Etape 5 du projet.
 *
 * @param timeStampNs : l'horodatage du message, en nanosecondes
 * @param icaoAddress : l'adresse OACI de l'expéditeur du message
 * @param category : la catégorie d'aéronef de l'expéditeur
 * @param callSign : l'indicatif de l'expéditeur
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message{

    /**
     * Les constantes qui suivent sont utilisées pour extraire les informations du message selon la procédure décrite dans l'énoncé du projet (voir le point 2.1 de l'Etape 5)
     */
    private static final int INVALID_CHAR = '?';
    private static final int CALL_SIGN_LENGTH = 8;
    private static final int CALL_SIGN_CHARACTER_LENGTH = 6;
    private static final int CA_LENGTH = 3;
    private static final int ME_CONTENT_LENGTH = 51;
    private static final int CATEGORY_MSB_CONSTANT = 14;    //voir point 2.1 de l'Etape 5 du projet (il n'y a aucune explication sur cette valeur)
    private static final int CA_TAKEN_SPACE_IN_CATEGORY = 4;

    /**
     * Constructeur compact
     * @param timeStampNs : l'horodatage du message, en nanosecondes
     * @param icaoAddress : l'adresse OACI de l'expéditeur du message
     * @param category : la catégorie d'aéronef de l'expéditeur
     * @param callSign : l'indicatif de l'expéditeur
     *
     * @throws NullPointerException si icaoAddress ou callSign sont nuls
     * @throws IllegalArgumentException si timeStampNs est strictement inférieure à 0
     */
    public AircraftIdentificationMessage{
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >=0);
    }


    /**
     * Classe publique statique retournant le message d'identification correspondant au message brut donné, ou null si au moins un des caractères de l'indicatif qu'il contient est invalide (voir point 2.1 de l'Etape 5 du projet).
     * @param rawMessage : le message brut à analyser
     * @return le message d'identification correspondant au message brut donné, ou null si au moins un des caractères de l'indicatif qu'il contient est invalide
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage){
        long attributME = rawMessage.payload();

        //traitement de la catégorie
        int CA = extractCallSignCharacter(attributME, 0);
        int typeCode = rawMessage.typeCode();   //ce type devrait appartenir à {1,2,3,4} ?
        int aeronefCategory = ((CATEGORY_MSB_CONSTANT - typeCode) << CA_TAKEN_SPACE_IN_CATEGORY | CA);  //voir point 2.1 de l'Etape 5 du projet


        //traitement de l'indicatif
        StringBuilder callSign = new StringBuilder();
        int Cint;
        char Cchar;
        for(int i = 1; i<=CALL_SIGN_LENGTH;++i) {
            Cint = extractCallSignCharacter(attributME, i);
            Cchar = intToCallSignCharacter(Cint);
            if(Cchar == INVALID_CHAR) { //si il y a un caractère invalide
                return null;
            }else if(Cchar != ' '){
                callSign.append(Cchar);
            }
        }
        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), Byte.toUnsignedInt((byte)aeronefCategory) , new CallSign(callSign.toString()));
    }

    /**
     * Retourne le caractère d'indice CharIndex de l'indicatif
     * @param attributME : le message brut à analyser
     * @param CharIndex : l'indice du caractère à retourner
     * @return le caractère d'indice CharIndex de l'indicatif
     * @throws IllegalArgumentException si CharIndex n'est pas compris entre 0 (compris) et 8 (compris)
     */
    private static int extractCallSignCharacter(long attributME, int CharIndex){
        if(CharIndex == 0) {
            return Bits.extractUInt(attributME, ME_CONTENT_LENGTH - CA_LENGTH, CA_LENGTH);
        }else if(CharIndex > 0 && CharIndex <= CALL_SIGN_LENGTH){
            return Bits.extractUInt(attributME, ME_CONTENT_LENGTH - CA_LENGTH - CharIndex * CALL_SIGN_CHARACTER_LENGTH, CALL_SIGN_CHARACTER_LENGTH);
        }else{
            throw new IllegalArgumentException();
        }
    }

    /**
     * Les constantes qui suivent sont simplement utilisées pour la conversion d'entiers en caractères.
     */
    private static final int A_VALUE = 65;
    private static final int ZERO_VALUE = 48;
    private static final int SPACE_VALUE = 32;
    private static final int NINE_VALUE = 57;
    private static final int ALPHABET_SIZE = 26;

    /**
     * Retourne le caractère correspondant à l'entier C dans la convention donnée au point 2.1 de l'Etape 5 du projet.
     * @param C : l'entier à convertir
     * @return le caractère correspondant à l'entier C dans la convention donnée au point 2.1 de l'Etape 5 du projet
     */
    private static char intToCallSignCharacter(int C){
        if(C > 0 && C <= ALPHABET_SIZE){
            return (char) ((C-1) + A_VALUE); //On fait C-1 car on veut un indice (et en informatique on commence à 0 et non à 1)
        }else if(C >= ZERO_VALUE && C <= NINE_VALUE){
            return (char) (C);
        }else if(C == SPACE_VALUE) {
            return ' ';
        }else{
            return INVALID_CHAR;
        }
    }
}
