package org.workcraft.plugins.wtg.commands;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.util.DialogUtils;

import java.util.*;

import static org.workcraft.plugins.wtg.commands.WtgUtils.*;
import static org.workcraft.plugins.wtg.converter.WtgToStgConverter.*;

public class WtgVerificationUtils {

    public static boolean checkNameCollisions(Wtg wtg) {
        //Check collisions between signal names and the dummy transitions that will be created in a conversion to STG
        if (!checkSignalNameCollision(wtg)) {
            return false;
        }
        //Check collisions between state names and the places for unstable signals
        return checkStateNameCollision(wtg);
    }

    private static boolean checkSignalNameCollision(Wtg wtg) {
        Set<String> dummyTransitions = new HashSet<>();
        for (TransitionEvent transition : wtg.getTransitions()) {
            if (transition.getDirection() == TransitionEvent.Direction.DESTABILISE) {
                dummyTransitions.add(getDestabiliseEventName(wtg.getName(transition.getSignal())));
            } else if (transition.getDirection() == TransitionEvent.Direction.STABILISE) {
                dummyTransitions.add(getStabiliseEventName(wtg.getName(transition.getSignal())));
            }
        }
        for (Waveform waveform : wtg.getWaveforms()) {
            dummyTransitions.add(getEntryEventName(wtg.getName(waveform)));
            dummyTransitions.add(getExitEventName(wtg.getName(waveform)));
        }

        for (String signalName : wtg.getSignalNames()) {
            if (dummyTransitions.contains(signalName)) {
                DialogUtils.showError("Signal " + signalName +
                        " has a conflictive name. Please rename the signal or change conversion suffix.");
                return false;
            }
        }

        return true;
    }

    private static boolean checkStateNameCollision(Wtg wtg) {
        Set<String> unstableSignalPlaces = new HashSet<>();
        for (String signalName : getUnstableSignalNames(wtg)) {
            unstableSignalPlaces.add(getLowStateName(signalName));
            unstableSignalPlaces.add(getHighStateName(signalName));
            unstableSignalPlaces.add(getStableStateName(signalName));
            unstableSignalPlaces.add(getUnstableStateName(signalName));
        }
        for (State state : wtg.getStates()) {
            String stateName = wtg.getName(state);
            if (unstableSignalPlaces.contains(stateName)) {
                DialogUtils.showError("State " + stateName +
                        " has a conflictive name. Please rename the state or change conversion suffix.");
                return false;
            }
        }

        return true;
    }

    public static boolean checkWtgStructure(Wtg wtg) {
        //Checks whether signal types are consistent accross different waveforms
        if (!checkConsistentSignalTypes(wtg)) {
            return false;
        }
        //Checks the preset and postset of every transition
        //(i.e. not empty, connected to another transition of the same signal in pre/postset)
        if (!checkTransitionsPresetPostset(wtg)) {
            return false;
        }
        //Checks the pre/postset of the exit event is correct (i.e. postset empty, preset connected correctly)
        if (!checkExitEventPresetPostset(wtg)) {
            return false;
        }
        //Checks the pre/postset of the entry event is correct (i.e. preset empty, postset connected correctly)
        if (!checkEntryEventPresetPostset(wtg)) {
            return false;
        }
        //Checks whether there is exactly one entry and exit event for every signal in every waveform
        if (!checkEntryExitEventsUniqueSignal(wtg)) {
            return false;
        }
        //Checks whether there are output transitions as the first event in a waveform following a choice
        if (!checkFirstTransitionIsValid(wtg)) {
            return false;
        }
        //Checks whether the initial value for signals matches the value of the guards
        if (!checkInitialValueForGuards(wtg)) {
            return false;
        }
        //Checks whether signals from a guard are used in the waveform
        if (!checkGuardedSignalsAreUsed(wtg)) {
            return false;
        }
        //Checks that the initial state has no guard
        if (!checkInitialStateHasNoGuard(wtg)) {
            return false;
        }
        //Checks that outputs never become unstable
        if (!checkOutputsAreStable(wtg)) {
            return false;
        }
        //Checks that guard functions are correct and complete
        if (!checkGuardFunctions(wtg)) {
            return false;
        }
        //Checks that signals are stable before reaching a guard
        if (!checkGuardSignalsAreStable(wtg)) {
            return false;
        }
        //Checks whether all states and waveforms are reachable from the initial state
        if (!checkNodeReachability(wtg)) {
            return false;
        }
        //Checks whether all waveforms have entry states
        if (!checkWaveformsHaveEntryState(wtg)) {
            return false;
        }
        //Checks whether all waveforms have exit states
        if (!checkWaveformsHaveExitState(wtg)) {
            return false;
        }
        //Checks that waveforms are only connected to states (no connections between waveforms)
        if (!checkWaveformsOnlyConnectToStates(wtg)) {
            return false;
        }
        //Checks that waveforms are not emtpy
        if (!checkWaveformsNotEmpty(wtg)) {
            return false;
        }
        //Checks that the initial signal states are valid (i.e. no signal stable at unknown)
        if (!checkValidInitialSignalStates(wtg)) {
            return false;
        }
        //Checks that stabilise/destabilise transitions never trigger an output
        if (!checkUnstableSignalTriggers(wtg)) {
            return false;
        }
        return true;
    }

    public static boolean checkConsistentSignalTypes(Wtg wtg) {
        Map<String, Signal.Type> signalTypes = new HashMap<>();
        for (Signal signal : wtg.getSignals()) {
            String signalName = wtg.getName(signal);
            Signal.Type signalType = signal.getType();
            if (signalTypes.containsKey(signalName)) {
                if (signalTypes.get(signalName) != signalType) {
                    DialogUtils.showError("Signal " + signalName + " has different types in different waveforms.");
                    return false;
                }
            } else {
                signalTypes.put(signalName, signalType);
            }
        }
        return true;
    }

    public static boolean checkTransitionsPresetPostset(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (TransitionEvent transition : wtg.getTransitions(waveform)) {
                Signal signal = transition.getSignal();
                Set<Node> preset = wtg.getPreset(transition);
                //Every transition has exactly one transition for "signal" in its preset
                if (!checkTransitionConnections(wtg, waveform, preset, signal, "preset")) {
                    return false;
                }
                //Every transition has exactly one transition for "signal" in its postset
                Set<Node> postset = wtg.getPostset(transition);
                if (!checkTransitionConnections(wtg, waveform, postset, signal, "postset")) {
                    return false;
                }
            }

        }
        return true;
    }

    private static boolean checkTransitionConnections(Wtg wtg, Waveform waveform, Set<Node> connections,
                                                       Signal signal, String connectionType) {
        if (connections.isEmpty()) {
            DialogUtils.showError("Transition for signal " + wtg.getName(signal) + " in waveform "
                    + wtg.getName(waveform) + " has an empty " + connectionType + ".");
            return false;
        }
        boolean signalInConnection = false;
        for (Node node : connections) {
            Event connectedTransition = (Event) node;
            if (connectedTransition.getSignal() == signal) {
                if (signalInConnection) {
                    DialogUtils.showError("Transition for signal " + wtg.getName(signal) + " in waveform "
                            + wtg.getName(waveform) + " has more than one transition for the same signal in its "
                            + connectionType + ".");
                    return false;
                }
                signalInConnection = true;
            }
        }
        if (!signalInConnection) {
            DialogUtils.showError("Transition for signal " + wtg.getName(signal) + " in waveform "
                    + wtg.getName(waveform) + " does not have a transition for the same signal in its "
                    + connectionType + ".");
            return false;
        }

        return true;
    }

    public static boolean checkExitEventPresetPostset(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (ExitEvent exit : wtg.getExits(waveform)) {

                if (!wtg.getPostset(exit).isEmpty()) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Exit event with a non-empty postset.");
                    return false;
                }
                Set<Node> preset = wtg.getPreset(exit);
                if (preset.isEmpty()) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Exit event with an empty preset.");
                    return false;
                }
                if (preset.size() > 1) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Exit event with more than one transition in its preset.");
                    return false;
                }
                if (((Event) preset.iterator().next()).getSignal() != exit.getSignal()) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Exit event connected to an event with a different signal.");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkEntryEventPresetPostset(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (EntryEvent entry : wtg.getEntries(waveform)) {
                if (!wtg.getPreset(entry).isEmpty()) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Entry event with a non-empty preset.");
                    return false;
                }
                Set<Node> postset = wtg.getPostset(entry);
                if (postset.isEmpty()) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Entry event with an empty postset.");
                    return false;
                }
                if (postset.size() > 1) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Entry event with more than one transition in its postset.");
                    return false;
                }
                if (((Event) postset.iterator().next()).getSignal() != entry.getSignal()) {
                    DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                            " has an Entry event connected to an event with a different signal.");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkEntryExitEventsUniqueSignal(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            //Check if every signal has exactly one entry event, and no other entry events exist
            Set<String> entryEventSignals = getEntryEventSignalNames(wtg, waveform);
            if (wtg.getEntries(waveform).size() > entryEventSignals.size()) {
                DialogUtils.showError("Waveform " + wtg.getName(waveform) + " has repeated Entry events.");
                return false;
            }
            if (!checkEventsMatchSignals(wtg, waveform, entryEventSignals, "Entry")) {
                return false;
            }

            //Check if every signal has exactly one exit event, and no other exit events exist
            Set<String> exitEventSignals = getExitEventSignalNames(wtg, waveform);
            if (wtg.getExits(waveform).size() > exitEventSignals.size()) {
                DialogUtils.showError("Waveform " + wtg.getName(waveform) + " has repeated Exit events.");
                return false;
            }
            if (!checkEventsMatchSignals(wtg, waveform, exitEventSignals, "Exit")) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkEventsMatchSignals(Wtg wtg, Waveform waveform, Set<String> eventSignals,
                                                   String eventType) {
        Collection<Signal> waveformSignals = wtg.getSignals(waveform);
        if (waveformSignals.size() < eventSignals.size()) {
            DialogUtils.showError("Waveform " + wtg.getName(waveform) +
                    " has " + eventType + " events for unknown signals.");
            return false;
        }
        for (Signal signal : waveformSignals) {
            String signalName = wtg.getName(signal);
            if (!eventSignals.contains(signalName)) {
                DialogUtils.showError(eventType + " event missing for signal " + signalName + " in waveform " +
                        wtg.getName(waveform) + ".");
                return false;
            }
        }
        return true;
    }

    public static boolean checkFirstTransitionIsValid(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (EntryEvent entry : wtg.getEntries(waveform)) {
                if (entry.getSignal().getType() == Signal.Type.OUTPUT) {
                    for (Node node: wtg.getPostset(entry)) {
                        if ((isFirstTransition(wtg, node)) && (isWaveformInChoice(wtg, waveform))) {
                            DialogUtils.showError("Output signal " + wtg.getName(entry.getSignal())
                                    + " cannot be fired immediately after a choice, in waveform "
                                    + wtg.getName(waveform) + ".");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean isWaveformInChoice(Wtg wtg, Waveform waveform) {
        return wtg.getPostset(wtg.getPreset(waveform).iterator().next()).size() > 1;
    }

    private static boolean isFirstTransition(Wtg wtg, Node node) {
        if (!(node instanceof ExitEvent)) {
            return wtg.getPreset(node).size() == 1;
        }
        return false;
    }



    public static boolean checkInitialValueForGuards(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (Signal signal : wtg.getSignals(waveform)) {
                String signalName = wtg.getName(signal);
                if (waveform.getGuard().containsKey(signalName)) {
                    Signal.State signalGuardState = (waveform.getGuard().get(signalName)) ?
                            Signal.State.HIGH : Signal.State.LOW;
                    if (signal.getInitialState() != signalGuardState) {
                        DialogUtils.showError("The initial state for signal " + signalName + " in waveform "
                                + wtg.getName(waveform) + " does not match the guard value.");
                        return false;
                    }
                }
            }

        }
        return true;
    }

    public static boolean checkGuardedSignalsAreUsed(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<String> signalsUsed = new HashSet<>();
            for (EntryEvent entry : wtg.getEntries(waveform)) {
                signalsUsed.add(wtg.getName(entry.getSignal()));
            }
            for (String signalGuard : waveform.getGuard().keySet()) {
                if (!signalsUsed.contains(signalGuard)) {
                    DialogUtils.showError("The guard from waveform " + wtg.getName(waveform) +
                            " is defined for signal " + signalGuard + ", but that signal is not used in the waveform.");
                    return false;
                }
            }

        }
        return true;
    }

    public static boolean checkInitialStateHasNoGuard(Wtg wtg) {
        State initialState = wtg.getInitialState();
        for (Node node : wtg.getPostset(initialState)) {
            if (node instanceof Waveform) {
                Waveform waveform = (Waveform) node;
                if (!waveform.getGuard().isEmpty()) {
                    DialogUtils.showError("The initial state cannot be succeeded by guards.");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkOutputsAreStable(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (Signal signal : wtg.getSignals(waveform)) {
                if (signal.getType() == Signal.Type.OUTPUT) {
                    String signalName = wtg.getName(signal);
                    if (waveform.getGuard().containsKey(signalName)) {
                        DialogUtils.showError("Output signal " + signalName + " cannot be in a guard.");
                        return false;
                    }
                    if ((signal.getInitialState() == Signal.State.UNSTABLE) ||
                            (signal.getInitialState() == Signal.State.STABLE)) {
                        DialogUtils.showError("Output signal " + signalName +
                                " has an illegal initial state in waveform " + wtg.getName(waveform) + ".");
                        return false;
                    }
                }
            }

            for (TransitionEvent transition : wtg.getTransitions(waveform)) {
                TransitionEvent.Direction direction = transition.getDirection();
                if ((direction == TransitionEvent.Direction.DESTABILISE) ||
                        (direction == TransitionEvent.Direction.STABILISE)) {
                    Signal signal = transition.getSignal();
                    if (signal.getType() == Signal.Type.OUTPUT) {
                        DialogUtils.showError("Output signal " + wtg.getName(signal) +
                                " has an illegal transition in waveform " + wtg.getName(waveform) + ".");
                        return false;
                    }
                }

            }
        }
        return true;
    }


    public static boolean checkGuardFunctions(Wtg wtg) {
        for (State state : wtg.getStates()) {
            if (!checkGuardFunctions(wtg, state)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkGuardFunctions(Wtg wtg, State state) {
        Map<String, Guard> guards = getGuardFromState(wtg, state);
        if (guards.isEmpty()) {
            return true;
        }

        Set<String> guardedSignals = new HashSet<>();
        for (Guard guard : guards.values()) {
            guardedSignals.addAll(guard.keySet());
        }
        if (guardedSignals.isEmpty()) {
            return true;
        }

        int expectedGuards = (int) Math.pow(2d, guardedSignals.size());
        if (guards.size() < expectedGuards) {
            DialogUtils.showError("There are missing waveforms in the postset of state "
                    + wtg.getName(state) + ".");
            return false;
        }

        Set<List<Boolean>> guardConditions = new HashSet<>();
        for (Map.Entry<String, Guard> guardEntry : guards.entrySet()) {
            Guard guard = guardEntry.getValue();
            if (guard.size() < guardedSignals.size()) {
                DialogUtils.showError("The guard from waveform " + guardEntry.getKey() + " is missing signals.");
                return false;
            }
            List<Boolean> signalValues = new ArrayList<>();
            for (String signalName : guardedSignals) {
                signalValues.add(guard.get(signalName));
            }
            if (guardConditions.contains(signalValues)) {
                DialogUtils.showError("The guard " + guard.toString() + " is repeated at state "
                        + wtg.getName(state) + ".");
                return false;
            }
            guardConditions.add(signalValues);
        }
        return true;
    }

    public static boolean checkGuardSignalsAreStable(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Guard guard = waveform.getGuard();
            if (!guard.isEmpty()) {
                Node precedingState = wtg.getPreset(waveform).iterator().next();
                if (wtg.getPreset(precedingState).isEmpty()) {
                    return false;
                } else {
                    for (Node predecessorWaveform : wtg.getPreset(precedingState)) {
                        Map<String, Signal.State> waveformFinalState = getFinalSignalStatesFromWaveform(wtg,
                                (Waveform) predecessorWaveform);
                        for (String signalGuarded : guard.keySet()) {
                            if (waveformFinalState.get(signalGuarded) != Signal.State.STABLE) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkNodeReachability(Wtg wtg) {
        Set<Node> reachableNodes = new HashSet<>();
        State initialState = wtg.getInitialState();
        int nonReachableNodes = wtg.getWaveforms().size() + wtg.getStates().size() - 1;
        Queue<Node> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(initialState);
        reachableNodes.add(initialState);
        //BFS
        while (!nodesToVisit.isEmpty()) {
            Node node = nodesToVisit.poll();
            for (Node n : wtg.getPostset(node)) {
                if (!reachableNodes.contains(n)) {
                    reachableNodes.add(n);
                    nodesToVisit.add(n);
                    nonReachableNodes = nonReachableNodes - 1;
                }
            }
        }

        if (nonReachableNodes > 0) {
            //Error handling
            String msg = "The following nodes are unreachable:\n";
            for (Waveform waveform : wtg.getWaveforms()) {
                if (!reachableNodes.contains(waveform)) {
                    msg = msg.concat("    " + wtg.getName(waveform) + "\n");
                }
            }
            for (State state : wtg.getStates()) {
                if (!reachableNodes.contains(state)) {
                    msg = msg.concat("    " + wtg.getName(state) + "\n");
                }
            }
            DialogUtils.showError(msg);
            return false;
        }
        return true;
    }

    public static boolean checkWaveformsHaveEntryState(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<Node> preset = wtg.getPreset(waveform);
            if (preset.size() != 1) {
                DialogUtils.showError("A waveform must have exactly one entry state.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkWaveformsHaveExitState(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<Node> postset = wtg.getPostset(waveform);
            if (postset.size() != 1) {
                DialogUtils.showError("A waveform must have exactly one exit state.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkWaveformsOnlyConnectToStates(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<Node> preset = wtg.getPreset(waveform);
            Set<Node> postset = wtg.getPostset(waveform);
            String msg = "A waveform can only be connected to states.";
            for (Node node : preset) {
                if (!(node instanceof State)) {
                    DialogUtils.showError(msg);
                    return false;
                }
            }
            for (Node node : postset) {
                if (!(node instanceof State)) {
                    DialogUtils.showError(msg);
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkWaveformsNotEmpty(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            if (wtg.getTransitions(waveform).isEmpty()) {
                DialogUtils.showError("Waveforms should have at least one transition.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkValidInitialSignalStates(Wtg wtg) {
        for (Signal.State signalState : getInitialSignalStates(wtg).values()) {
            if (signalState == Signal.State.STABLE) {
                DialogUtils.showError("The initial state for a signal in a WTG can not be " +
                        Signal.State.STABLE.toString());
                return false;
            }
        }
        return true;
    }

    public static boolean checkUnstableSignalTriggers(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (TransitionEvent transition : wtg.getTransitions(waveform)) {
                TransitionEvent.Direction direction = transition.getDirection();
                if ((wtg.getPreviousState(transition) == Signal.State.UNSTABLE) ||
                        (direction == TransitionEvent.Direction.DESTABILISE)) {
                    for (Node node : wtg.getPostset(transition)) {
                        if (node instanceof TransitionEvent) {
                            TransitionEvent dstTransition = (TransitionEvent) node;
                            Signal signal = dstTransition.getSignal();
                            if (signal.getType() != Signal.Type.INPUT) {
                                String transitionName = wtg.getName(transition.getSignal()) + direction.getSymbol();

                                String msg = "Transition " + transitionName + " triggers the signal ";
                                msg = msg + wtg.getName(signal) +
                                        ". Stabilising/destabilising transitions should only trigger inputs";
                                DialogUtils.showError(msg);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkConsistency(Wtg wtg) {
        Map<String, Signal.State> initialSignalStates = getInitialSignalStates(wtg);
        State initialState = wtg.getInitialState();
        Map<State, Map<String, Signal.State>> signalStates = new HashMap<>();
        Queue<Node> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(initialState);
        signalStates.put(initialState, initialSignalStates);
        while (!nodesToVisit.isEmpty()) {
            Node node = nodesToVisit.poll();

            if (node instanceof State) {
                nodesToVisit.addAll(wtg.getPostset(node));
            } else if (node instanceof Waveform) {
                Waveform waveform = (Waveform) node;
                Node predecessorNode = wtg.getPreset(waveform).iterator().next();
                if (predecessorNode instanceof State) {
                    if ((!checkConsistency(wtg, waveform)) ||
                            (!checkInitialSignalStateConsistency(wtg, waveform, signalStates.get(predecessorNode)))) {
                        return false;
                    }
                } else {
                    throw new FormatException("Incorrect preset of waveform '" + wtg.getName(waveform) + "'");
                }

                Map<String, Signal.State> finalWaveformSignalState = new HashMap<>(signalStates.get(predecessorNode));
                finalWaveformSignalState.putAll(getFinalSignalStatesFromWaveform(wtg, waveform));
                Node successorNode = wtg.getPostset(waveform).iterator().next();
                if (successorNode instanceof State) {
                    if (signalStates.containsKey(successorNode)) {
                        if (!signalStates.get(successorNode).equals(finalWaveformSignalState)) {
                            return false;
                        }
                    } else {
                        signalStates.put((State) successorNode, finalWaveformSignalState);
                        nodesToVisit.add(successorNode);
                    }
                } else {
                    throw new FormatException("Incorrect postset of waveform '" + wtg.getName(waveform) + "'");
                }

            }
        }
        return true;
    }

    private static boolean checkInitialSignalStateConsistency(Wtg wtg, Waveform waveform, Map<String, Signal.State> signalStates) {
        for (Signal signal : wtg.getSignals(waveform)) {
            String signalName = wtg.getName(signal);
            Signal.State initialSignalState = signal.getInitialState();
            if (initialSignalState != signalStates.get(signalName)) {
                Guard guard = waveform.getGuard();
                if ((signalStates.get(signalName) == Signal.State.STABLE) && (guard.containsKey(signalName))) {
                    Boolean guardValue = waveform.getGuard().get(signalName);
                    if ((guardValue && initialSignalState != Signal.State.HIGH) ||
                            (!guardValue && initialSignalState != Signal.State.LOW)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkConsistency(Wtg wtg, Waveform waveform) {
        for (TransitionEvent transition : wtg.getTransitions(waveform)) {
            TransitionEvent.Direction direction = transition.getDirection();
            Signal.State previousState = wtg.getPreviousState(transition);
            if (direction == TransitionEvent.Direction.RISE) {
                if ((previousState != Signal.State.LOW) && (previousState != Signal.State.UNSTABLE)) {
                    return false;
                }
            } else if (direction == TransitionEvent.Direction.FALL) {
                if ((previousState != Signal.State.HIGH) && (previousState != Signal.State.UNSTABLE)) {
                    return false;
                }
            }  else if (direction == TransitionEvent.Direction.STABILISE) {
                if (previousState != Signal.State.UNSTABLE) {
                    return false;
                }
            } else if (direction == TransitionEvent.Direction.DESTABILISE) {
                if ((previousState != Signal.State.HIGH) && (previousState != Signal.State.LOW)) {
                    return false;
                }
            }
        }
        return true;
    }
}
