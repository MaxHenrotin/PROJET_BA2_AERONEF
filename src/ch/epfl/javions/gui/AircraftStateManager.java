package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

/**
 * Classe ayant pour but de garder à jour les états d'un ensemble d'aéronefs en fonction des messages reçus d'eux.
 * Une de ses instances gérera les états de la totalité des aéronefs visibles sur la carte.
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class AircraftStateManager {

    private final static long ONE_MINUTE = (long) 60e9;

    /**
     * table associant un accumulateur d'état d'aéronef à l'adresse OACI de tout aéronef dont un message a été reçu récemment
     */
    private Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> managementTable = new HashMap<>();

    /**
     * Ensemble (observable) des états de tous les aéronefs dont la position est connue
     * (en gros représente la liste de tous les aéronefs avec lesquels on est en train d'intéragir)
     */

    private ObservableSet<ObservableAircraftState> observableAircraftStates;

    private ObservableSet<ObservableAircraftState> viewOfObservableAircraftStates;

    private long lastMessageTimeStampsNs;

    private AircraftDatabase aircraftDatabase;
    public AircraftStateManager(AircraftDatabase aircraftDatabase){
        this.aircraftDatabase = aircraftDatabase;

        observableAircraftStates = FXCollections.observableSet();
        viewOfObservableAircraftStates = FXCollections.unmodifiableObservableSet(observableAircraftStates);
        lastMessageTimeStampsNs = -1;
    }

    public ObservableSet<ObservableAircraftState> states(){
        return viewOfObservableAircraftStates;
    }

    public void updateWithMessage(Message message) throws IOException {
        if(message != null) {
            IcaoAddress messageAdress = message.icaoAddress();
            lastMessageTimeStampsNs = message.timeStampNs();

            if (managementTable.containsKey(messageAdress)) {

                managementTable.get(messageAdress).update(message);

            } else {
                ObservableAircraftState observableAircraftState = new ObservableAircraftState
                        (messageAdress,
                                aircraftDatabase.get(messageAdress));

                observableAircraftStates.add(observableAircraftState);
                managementTable.put(messageAdress,
                        new AircraftStateAccumulator<>
                                (observableAircraftState));
            }
        }
    }

    public void purge(){
        for (ObservableAircraftState observableAircraftState : observableAircraftStates) {
            if(lastMessageTimeStampsNs - observableAircraftState.getLastMessageTimeStampsNs() >= ONE_MINUTE){
                observableAircraftStates.remove(observableAircraftState);
            }
        }
    }
}
