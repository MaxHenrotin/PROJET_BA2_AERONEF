package ch.epfl.javions.gui;
//  Author:    Max Henrotin

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Classe ayant pour but de garder à jour les états d'un ensemble d'aéronefs en fonction des messages reçus d'eux.
 * Une de ses instances gérera les états de la totalité des aéronefs visibles sur la carte.
 *
 * @author Julien Erbland (346893)
 * @author Max Henrotin (341463)
 */

public final class AircraftStateManager {

    /**
     * table associant un accumulateur d'état d'aéronef à l'adresse OACI de tout aéronef dont un message a été reçu récemment
     */
    private Map< AircraftStateAccumulator<AircraftStateSetter> , IcaoAddress> managementTable = new HashMap<>();

    /**
     * ensemble (observable) des états des aéronefs dont la position est connue
     */
    private Set<ObservableAircraftState> observableAircraftStates = new HashSet<>();

}
