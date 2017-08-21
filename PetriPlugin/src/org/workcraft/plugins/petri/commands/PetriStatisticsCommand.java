package org.workcraft.plugins.petri.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.commands.AbstractStatisticsCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetriStatisticsCommand extends AbstractStatisticsCommand {

    @Override
    public String getDisplayName() {
        return "Petri net analysis";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNet.class);
    }

    @Override
    public String getStatistics(WorkspaceEntry we) {
        PetriNet petri = WorkspaceUtils.getAs(we, PetriNet.class);

        Collection<Transition> transitions = petri.getTransitions();
        Collection<Place> places = petri.getPlaces();
        Collection<Connection> connections = petri.getConnections();

        int producingArcCount = 0;
        int consumingArcCount = 0;
        for (Connection connection: connections) {
            if (connection.getFirst() instanceof Transition) {
                producingArcCount++;
            }
            if (connection.getFirst() instanceof Place) {
                consumingArcCount++;
            }
        }

        int forkCount = 0;
        int joinCount = 0;
        int sourceTransitionCount = 0;
        int sinkTransitionCount = 0;
        int isolatedTransitionCount = 0;
        int maxTransitionFanin = 0;
        int maxTransitionFanout = 0;
        int selfLoopCount = 0;
        for (Transition transition: transitions) {
            Set<Node> transitionPreset = petri.getPreset(transition);
            Set<Node> transitionPostset = petri.getPostset(transition);
            if (transitionPreset.size() > 1) {
                joinCount++;
            }
            if (transitionPostset.size() > 1) {
                forkCount++;
            }
            if (transitionPreset.isEmpty()) {
                sourceTransitionCount++;
            }
            if (transitionPostset.isEmpty()) {
                sinkTransitionCount++;
            }
            if (transitionPreset.isEmpty() && transitionPostset.isEmpty()) {
                isolatedTransitionCount++;
            }
            if (transitionPreset.size() > maxTransitionFanin) {
                maxTransitionFanin = transitionPreset.size();
            }
            if (transitionPostset.size() > maxTransitionFanout) {
                maxTransitionFanout = transitionPostset.size();
            }
            HashSet<Node> loopset = new HashSet<>(transitionPreset);
            loopset.retainAll(transitionPostset);
            selfLoopCount += loopset.size();
        }

        int choiceCount = 0;
        int mergeCount = 0;
        int sourcePlaceCount = 0;
        int sinkPlaceCount = 0;
        int isolatedPlaceCount = 0;
        int maxPlaceFanin = 0;
        int maxPlaceFanout = 0;
        int tokenCount = 0;
        int markedCount = 0;
        for (Place place: places) {
            Set<Node> placePreset = petri.getPreset(place);
            Set<Node> placePostset = petri.getPostset(place);
            if (placePreset.size() > 1) {
                mergeCount++;
            }
            if (placePostset.size() > 1) {
                choiceCount++;
            }
            if (placePreset.isEmpty()) {
                sourcePlaceCount++;
            }
            if (placePostset.isEmpty()) {
                sinkPlaceCount++;
            }
            if (placePreset.isEmpty() && placePostset.isEmpty()) {
                isolatedPlaceCount++;
            }
            if (placePreset.size() > maxPlaceFanin) {
                maxPlaceFanin = placePreset.size();
            }
            if (placePostset.size() > maxPlaceFanout) {
                maxPlaceFanout = placePostset.size();
            }
            if (place.getTokens() > 0) {
                tokenCount += place.getTokens();
                markedCount++;
            }
        }

        return "Petri net analysis:"
                + "\n  Transition count -  " + transitions.size()
                + "\n    * Fork / join -  " + forkCount + " / " + joinCount
                + "\n    * Source / sink -  " + sourceTransitionCount + " / " + sinkTransitionCount
                + "\n    * Max fanin / fanout -  " + maxTransitionFanin + " / " + maxTransitionFanout
                + "\n  Place count -  " + places.size()
                + "\n    * Choice / merge -  " + choiceCount + " / " + mergeCount
                + "\n    * Source / sink  -  " + sourcePlaceCount + " / " + sinkPlaceCount
                + "\n    * Max fanin / fanout -  " + maxPlaceFanin + " / " + maxPlaceFanout
                + "\n    * Token / marked -  " + tokenCount + " / " + markedCount
                + "\n  Arc count -  " + connections.size()
                + "\n    * Producing / consuming -  " + producingArcCount + " / " + consumingArcCount
                + "\n    * Self-loop -  " + selfLoopCount
                + "\n  Disconnected transitions / places -  " + isolatedTransitionCount + " / " + isolatedPlaceCount;
    }

}
