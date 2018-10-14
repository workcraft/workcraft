package org.workcraft.plugins.stg.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.commands.AbstractStatisticsCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.utils.PetriNetChecker;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StgStatisticsCommand extends AbstractStatisticsCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph analysis";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public String getStatistics(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        Collection<Transition> transitions = stg.getTransitions();
        Collection<Place> places = stg.getPlaces();
        Collection<Connection> connections = stg.getConnections();

        int inputSignalCount = stg.getSignalReferences(Signal.Type.INPUT).size();
        int outputSignalCount = stg.getSignalReferences(Signal.Type.OUTPUT).size();
        int internalSignalCount = stg.getSignalReferences(Signal.Type.INTERNAL).size();
        int signalCount = inputSignalCount + outputSignalCount + internalSignalCount;

        int inputTransitionCount = 0;
        int outputTransitionCount = 0;
        int internalTransitionCount = 0;
        int dummyTransitionCount = 0;
        int plusTransitionCount = 0;
        int minusTransitionCount = 0;
        int toggleTransitionCount = 0;
        int forkCount = 0;
        int joinCount = 0;
        int sourceTransitionCount = 0;
        int sinkTransitionCount = 0;
        int isolatedTransitionCount = 0;
        int maxTransitionFanin = 0;
        int maxTransitionFanout = 0;
        int selfLoopCount = 0;
        for (Transition transition: transitions) {
            if (transition instanceof SignalTransition) {
                SignalTransition signalTransition = (SignalTransition) transition;
                switch (signalTransition.getSignalType()) {
                case INPUT:
                    inputTransitionCount++;
                    break;
                case OUTPUT:
                    outputTransitionCount++;
                    break;
                case INTERNAL:
                    internalTransitionCount++;
                    break;
                }
                switch (signalTransition.getDirection()) {
                case PLUS:
                    plusTransitionCount++;
                    break;
                case MINUS:
                    minusTransitionCount++;
                    break;
                case TOGGLE:
                    toggleTransitionCount++;
                    break;
                }
            }
            if (transition instanceof DummyTransition) {
                dummyTransitionCount++;
            }
            Set<Node> transitionPreset = stg.getPreset(transition);
            Set<Node> transitionPostset = stg.getPostset(transition);
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
            Set<Node> placePreset = stg.getPreset(place);
            Set<Node> placePostset = stg.getPostset(place);
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

        return "Signal Transition Graph analysis:"
                + "\n  Signal count -  " + signalCount
                + "\n    * Input / output / internal -  " + inputSignalCount + " / " + outputSignalCount
                + " / " + internalSignalCount
                + "\n  Transition count -  " + transitions.size()
                + "\n    * Input / output / internal / dummy -  " + inputTransitionCount + " / " + outputTransitionCount
                + " / " + internalTransitionCount + " / " + dummyTransitionCount
                + "\n    * Rising / falling / toggle -  " + plusTransitionCount + " / " + minusTransitionCount
                + " / " + toggleTransitionCount
                + "\n    * Fork / join -  " + forkCount + " / " + joinCount
                + "\n    * Source / sink -  " + sourceTransitionCount + " / " + sinkTransitionCount
                + "\n    * Max fanin / fanout -  " + maxTransitionFanin + " / " + maxTransitionFanout
                + "\n  Place count -  " + places.size()
                + "\n    * Choice / merge -  " + choiceCount + " / " + mergeCount
                + "\n    * Source / sink -  " + sourcePlaceCount + " / " + sinkPlaceCount
                + "\n    * Mutex -  " + stg.getMutexPlaces().size()
                + "\n    * Max fanin / fanout -  " + maxPlaceFanin + " / " + maxPlaceFanout
                + "\n  Arc count -  " + connections.size()
                + "\n    * Producing / consuming -  " + producingArcCount + " / " + consumingArcCount
                + "\n    * Self-loop -  " + selfLoopCount
                + "\n  Token count / marked places -  " + tokenCount + " / " + markedCount
                + "\n  Isolated transitions / places -  " + isolatedTransitionCount + " / " + isolatedPlaceCount
                + "\n  Net type:"
                + "\n    * Marked graph -  " + PetriNetChecker.isMarkedGraph(stg)
                + "\n    * State machine -  " + PetriNetChecker.isStateMachine(stg)
                + "\n    * Free choice -  " + PetriNetChecker.isFreeChoice(stg)
                + "\n    * Extended free choice -  " + PetriNetChecker.isExtendedFreeChoice(stg)
                + "\n    * Pure -  " + PetriNetChecker.isPure(stg)
                + "\n";
    }

}
