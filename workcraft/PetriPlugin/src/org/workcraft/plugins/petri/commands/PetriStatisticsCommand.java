package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractStatisticsCommand;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.utils.CheckUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PetriStatisticsCommand extends AbstractStatisticsCommand {

    @Override
    public String getDisplayName() {
        return "Petri net analysis";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Petri.class);
    }

    @Override
    public String getStatistics(WorkspaceEntry we) {
        Petri petri = WorkspaceUtils.getAs(we, Petri.class);

        Collection<Transition> transitions = petri.getTransitions();
        Collection<Place> places = petri.getPlaces();
        Collection<MathConnection> connections = petri.getConnections();

        int producingArcCount = 0;
        int consumingArcCount = 0;
        for (MathConnection connection: connections) {
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
            Set<MathNode> transitionPreset = petri.getPreset(transition);
            Set<MathNode> transitionPostset = petri.getPostset(transition);
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
            HashSet<MathNode> loopset = new HashSet<>(transitionPreset);
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
            Set<MathNode> placePreset = petri.getPreset(place);
            Set<MathNode> placePostset = petri.getPostset(place);
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
                + "\n  Arc count -  " + connections.size()
                + "\n    * Producing / consuming -  " + producingArcCount + " / " + consumingArcCount
                + "\n    * Self-loop -  " + selfLoopCount
                + "\n  Token count / marked places -  " + tokenCount + " / " + markedCount
                + "\n  Isolated transitions / places -  " + isolatedTransitionCount + " / " + isolatedPlaceCount
                + "\n  Net type:"
                + "\n    * Marked graph -  " + CheckUtils.isMarkedGraph(petri)
                + "\n    * State machine -  " + CheckUtils.isStateMachine(petri)
                + "\n    * Free choice -  " + CheckUtils.isFreeChoice(petri)
                + "\n    * Extended free choice -  " + CheckUtils.isExtendedFreeChoice(petri)
                + "\n    * Pure -  " + CheckUtils.isPure(petri)
                + "\n";
    }

}
