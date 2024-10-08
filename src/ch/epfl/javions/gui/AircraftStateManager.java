package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
    private final static long ONE_MINUTE = (long) Units.convert(1,Units.Time.MINUTE,Units.Time.NANO_SECOND);

    //===================================== Attributs privées ==========================================================

    /*
     * table associant un accumulateur d'état d'aéronef à l'adresse OACI de tout aéronef
     *  dont un message a été reçu
     */
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> managementTable;
    private final AircraftDatabase aircraftDatabase;

    //Ensemble observable des états des aéronefs dont la position est connue
    private final ObservableSet<ObservableAircraftState> observableAircraftStates;
    //Vue non modifiable de l'ensemble observable des états des aéronefs dont la position est connue
    private final ObservableSet<ObservableAircraftState> viewOfObservableAircraftStates;
    private long lastMessageTimeStampsNs;

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
     * @throws IOException : si erreur de lecture
     */
    public void updateWithMessage(Message message) throws IOException {
        if(message != null) {
            IcaoAddress icaoAddress = message.icaoAddress();
            lastMessageTimeStampsNs = message.timeStampNs();

            //si l'aéronef n'est pas dans la table, on le rajoute et on crée un accumulateur d'état pour lui
            if (!managementTable.containsKey(icaoAddress)) {
                ObservableAircraftState observableAircraftState = new ObservableAircraftState
                                                                        (icaoAddress,
                                                                                aircraftDatabase.get(icaoAddress));

                AircraftStateAccumulator<ObservableAircraftState> newAccumulator =
                                                                new AircraftStateAccumulator<>(observableAircraftState);
                managementTable.put(icaoAddress, newAccumulator);
            }

            //on met à jour l'état de l'aéronef avec le message
            ObservableAircraftState currentObservableStateSetter = managementTable.get(icaoAddress).stateSetter();
            managementTable.get(icaoAddress).update(message);

            //si l'aéronef a une position, on l'ajoute à l'ensemble des états observables
            if(currentObservableStateSetter.getPosition() != null)
                observableAircraftStates.add(currentObservableStateSetter);
            }

    }

    /**
     * Supprime de l'ensemble des états observables tous ceux correspondant à des aéronefs
     * dont aucun message n'a été reçu dans la minute précédant la réception du dernier message
     * passé à updateWithMessage
     */
    public void purge(){
        Iterator<ObservableAircraftState> iterator = observableAircraftStates.iterator();
        while (iterator.hasNext()) {
            ObservableAircraftState state = iterator.next();
            if (lastMessageTimeStampsNs - state.getLastMessageTimeStampsNs() >= ONE_MINUTE) {
                iterator.remove();
                managementTable.remove(state.getIcaoAddress());
            }
        }
    }
}
