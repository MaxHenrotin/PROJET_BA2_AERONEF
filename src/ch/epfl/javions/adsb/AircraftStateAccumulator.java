package ch.epfl.javions.adsb;
//  Author:    Max Henrotin

import ch.epfl.javions.GeoPos;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Classe représentant un «accumulateur d'état d'aéronef», c.-à-d. un objet accumulant les messages ADS-B provenant d'un seul aéronef afin de déterminer son état au cours du temps
 * (C'est une classe générique associée à un objet de type AircraftStateSetter représentant l'état modifiable d'un aéronef)
 * Le rôle principal de l'accumulateur est d'appeler les méthodes de modification de cet état afin de le maintenir à jour au fur et à mesure de l'arrivée des messages envoyés par l'aéronef.)
 *
 * @param <T> : objet représentant l'état modifiable d'un aéronef (borné par AircraftStateSetter)
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public class AircraftStateAccumulator<T extends AircraftStateSetter> {  //extends impose que T soit une sous-classe de AircraftStateSetter (T est borné par AircraftStateSetter)

    private final T stateSetter;

   private AirbornePositionMessage[] messagesPositions = new AirbornePositionMessage[2];

   private int messagePositionParity;

   private final static double TIME_LIMIT_POSITION = 10000; //il faut 10 secondes d'écart max mais le timeStamps est exprimé en microsecondes

    /**
     * Constructeur retournant un accumulateur d'état d'aéronef associé à l'état modifiable donné
     * @param stateSetter : état modifiable de l'aéronef à associer à l'accumulateur
     */
    public AircraftStateAccumulator(T stateSetter){
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    /**
     * Getter de l'état modifiable de l'aéronef passé à son constructeur
     * @return l'état modifiable de l'aéronef passé à son constructeur
     */
    public T stateSetter(){ return stateSetter; }

    /**
     * Met à jour l'état de l'aéronef en fonction du message donné
     * @param message : message contenant les informations sur l'aéronef pour les mises à jour
     */
    public void update(Message message){

        switch (message){
            case AircraftIdentificationMessage messageIdectification :
                stateSetter.setCategory(messageIdectification.category());
                stateSetter.setCallSign(messageIdectification.callSign());
                break;
            case AirborneVelocityMessage messageVelocity :
                stateSetter.setVelocity(messageVelocity.speed());
                stateSetter.setTrackOrHeading(messageVelocity.trackOrHeading());
                break;
            case AirbornePositionMessage messagePosition :
                messagePositionParity = messagePosition.parity();
                messagesPositions[messagePositionParity] = messagePosition;

                stateSetter.setAltitude(messagePosition.altitude());

                if(checkValidPosition()){
                    double x0 = messagesPositions[0].x();
                    double y0 = messagesPositions[0].y();
                    double x1 = messagesPositions[1].x();
                    double y1 = messagesPositions[1].y();

                    GeoPos newPosition = CprDecoder.decodePosition(x0, y0, x1, y1, messagePositionParity);
                    stateSetter.setPosition(newPosition);
                }
                break;
            default:
                throw new IllegalArgumentException("Aucun message de type conforme reçu");
        }

    }

    private boolean checkValidPosition(){
        if(messagesPositions[0] != null && messagesPositions[1] != null){
            return Math.abs(messagesPositions[0].timeStampNs() - messagesPositions[1].timeStampNs()) < TIME_LIMIT_POSITION;
        }else {
            return false;
        }
    }

}
