package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import java.util.Objects;

/**
 * Classe représentant un «accumulateur d'état d'aéronef», c.-à-d. un objet accumulant les messages ADS-B
 * provenant d'un seul aéronef afin de déterminer son état au cours du temps (C'est une classe générique associée
 * à un objet de type AircraftStateSetter représentant l'état modifiable d'un aéronef)
 * Le rôle principal de l'accumulateur est d'appeler les méthodes de modification de cet état
 * afin de le maintenir à jour au fur et à mesure de l'arrivée des messages envoyés par l'aéronef.
 *
 * @param <T> : objet représentant l'état modifiable d'un aéronef (borné par AircraftStateSetter)
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public class AircraftStateAccumulator<T extends AircraftStateSetter> {
    //extends impose que T soit une sous-classe de AircraftStateSetter (T est borné par AircraftStateSetter)


    //===================================== Attributs privées statiques ================================================

    private final static int ODD_INDEX = 1;
    private final static int EVEN_INDEX = 0;

    // il faut 10 secondes d'écart max mais le timeStamps est exprimé en microsecondes
    private final static double TIME_LIMIT_POSITION = 10e9;

    //===================================== Attributs privées ==========================================================

    private final T stateSetter;
    private final AirbornePositionMessage[] messagesPositions = new AirbornePositionMessage[2];

    //===================================== Méthodes privées ===========================================================

    private boolean checkValidPosition(){
        if(messagesPositions[EVEN_INDEX] != null && messagesPositions[ODD_INDEX] != null){
            return Math.abs(messagesPositions[EVEN_INDEX].timeStampNs() - messagesPositions[ODD_INDEX].timeStampNs())
                    < TIME_LIMIT_POSITION;
        }else {
            return false;
        }
    }

    //===================================== Méthodes publiques =========================================================

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

        switch (message) {
            //vérifie le type du message et en crée une instance
            case AircraftIdentificationMessage messageIdentification -> { //message d'identification
                //met à jour le timestamps
                stateSetter.setLastMessageTimeStampNs(messageIdentification.timeStampNs());

                //met à jour la catégorie et le call sign
                stateSetter.setCategory(messageIdentification.category());
                stateSetter.setCallSign(messageIdentification.callSign());


            }
            case AirborneVelocityMessage messageVelocity -> { //message de vitesse et direction
                // met à jour le timestamps
                stateSetter.setLastMessageTimeStampNs(messageVelocity.timeStampNs());

                //met à jour la vitesse et la direction
                stateSetter.setVelocity(messageVelocity.speed());
                stateSetter.setTrackOrHeading(messageVelocity.trackOrHeading());
            }
            case AirbornePositionMessage messagePosition -> { //message de position

                //actualise les deux derniers messages reçu avec celui reçu en paramètre
                int messagePositionParity = messagePosition.parity();
                messagesPositions[messagePositionParity] = messagePosition;

                //met à jour l'altitude et le time stamps
                stateSetter.setLastMessageTimeStampNs(messagePosition.timeStampNs());
                stateSetter.setAltitude(messagePosition.altitude());

                if (checkValidPosition()) { //met à jour la position si elle est valide
                    double xEven = messagesPositions[EVEN_INDEX].x();
                    double yEven = messagesPositions[EVEN_INDEX].y();
                    double xOdd = messagesPositions[ODD_INDEX].x();
                    double yOdd = messagesPositions[ODD_INDEX].y();

                    GeoPos newPosition = CprDecoder.decodePosition(xEven, yEven, xOdd, yOdd, messagePositionParity);

                    //la nouvelle position peut être null en cas de changement de bande de latitude
                    if (newPosition != null) stateSetter.setPosition(newPosition);
                }
            }

            default -> throw new Error("Un type de message inconnu a été intercepté");
        }
    }
}
