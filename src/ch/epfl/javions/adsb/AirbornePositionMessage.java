package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
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
 */

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress,double altitude, int parity, double x, double y) implements Message{

    private static final int Q_INDEX = 4;

    private static final int Q_MASK = 1<<Q_INDEX;

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


        if(validAltitude){
            return AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitude, parity, x, y);
        }else {
            return null;
        }
    }


    private static boolean calculAltitude(int alt){

        if((alt & Q_MASK) == Q_MASK){ //Q = 1
            
        }else{ //Q = 0

        }

    }

}
