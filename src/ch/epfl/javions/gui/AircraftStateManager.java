package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe ayant pour but de garder à jour les états d'un ensemble d'aéronefs en fonction des messages reçus d'eux.
 * Une de ses instances gérera les états de la totalité des aéronefs visibles sur la carte.
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class AircraftStateManager {

    //===================================== Attributs privées statiques ================================================
    private final static long ONE_MINUTE = (long) 60e9;

    //===================================== Attributs privées ==========================================================

    /*
     * table associant un accumulateur d'état d'aéronef à l'adresse OACI de tout aéronef
     *  dont un message a été reçu récemment
     */
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> managementTable;

    /**
     * Ensemble (observable) des états de tous les aéronefs dont la position est connue
     * (en gros représente la liste de tous les aéronefs avec lesquels on est en train d'intéragir)
     */
    private long lastMessageTimeStampsNs;
    private final AircraftDatabase aircraftDatabase;
    private final ObservableSet<ObservableAircraftState> observableAircraftStates;

    private final ObservableSet<ObservableAircraftState> viewOfObservableAircraftStates;

    //===================================== Méthodes publiques =========================================================

    /**
     * Constructeur
     * @param aircraftDatabase : ensemble de données sur des avions
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase){
        this.aircraftDatabase = aircraftDatabase;

        managementTable = new HashMap<>();

        observableAircraftStates = FXCollections.observableSet();
        viewOfObservableAircraftStates = FXCollections.unmodifiableObservableSet(observableAircraftStates);

        lastMessageTimeStampsNs = -1;
    }

    /**
     * Retourne l'ensemble observable, mais non modifiable,
     * des états observables des aéronefs dont la position est connue
     * @return une vue sur les états observables
     */
    public ObservableSet<ObservableAircraftState> states(){
        return viewOfObservableAircraftStates;
    }

    /**
     * Prend un message et l'utilise pour mettre à jour l'état de l'aéronef qui l'a envoyé,
     * créant cet état lorsque le message est le premier reçu de cet aéronef
     * @param message : messaage reçu d'un aéronef
     * @throws IOException
     */
    public void updateWithMessage(Message message) throws IOException {
        if(message != null) {
            IcaoAddress messageAdress = message.icaoAddress();
            lastMessageTimeStampsNs = message.timeStampNs();

            if (!managementTable.containsKey(messageAdress)) {
                ObservableAircraftState observableAircraftState = new ObservableAircraftState
                                                                        (messageAdress,
                                                                            aircraftDatabase.get(messageAdress));

                AircraftStateAccumulator<ObservableAircraftState> newAccumulator =
                                                                new AircraftStateAccumulator<>(observableAircraftState);
                managementTable.put(messageAdress, newAccumulator);

            }

            ObservableAircraftState currentObservableStateSetter = managementTable
                                                                    .get(messageAdress)
                                                                    .stateSetter();

            managementTable.get(messageAdress).update(message);

            if(currentObservableStateSetter.getPosition() != null) observableAircraftStates
                                                                                    .add(currentObservableStateSetter);

            }

    }

    /**
     * Supprime de l'ensemble des états observables tous ceux correspondant à des aéronefs
     * dont aucun message n'a été reçu dans la minute précédant la réception du dernier message
     * passé à updateWithMessage
     */
    public void purge(){
        observableAircraftStates.removeIf(observableAircraftState ->
                lastMessageTimeStampsNs - observableAircraftState.getLastMessageTimeStampsNs() >= ONE_MINUTE);
    }
}
