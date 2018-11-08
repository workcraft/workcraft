package org.workcraft.plugins.wtg.utils;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;

import java.util.*;

import static org.workcraft.plugins.wtg.converter.WtgToStgConverter.*;
import static org.workcraft.plugins.wtg.utils.WtgUtils.*;

public class VerificationUtils {

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
                        " has a conflicting name. Please rename the signal or change conversion suffix.");
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
                DialogUtils.showError("State '" + stateName +
                        "' has a conflicting name. Please rename the state or change conversion suffix.");
                return false;
            }
        }

        return true;
    }

    public static boolean checkStructure(Wtg wtg) {
        //Checks whether the WTG has defined an initial state
        if (!checkInitialState(wtg)) {
            return false;
        }
        //Checks whether signal types are consistent across different waveforms
        if (!checkConsistentSignalTypes(wtg)) {
            return false;
        }
        //Checks the preset and postset of every transition
        //(i.e. not empty, connected to another transition of the same signal in preset and postset)
        if (!checkTransitionsPresetPostset(wtg)) {
            return false;
        }
        //Checks the preset and postset of the exit event is correct (i.e. postset empty, preset connected correctly)
        if (!checkExitEventPresetPostset(wtg)) {
            return false;
        }
        //Checks the preset and postset of the entry event is correct (i.e. preset empty, postset connected correctly)
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
        //Checks that the initial signal states are valid (i.e. no signal stable at unknown or unstable)
        if (!checkValidInitialSignalStates(wtg)) {
            return false;
        }
        //Checks that stabilise/destabilise transitions never trigger an output
        if (!checkUnstableSignalTriggers(wtg)) {
            return false;
        }
        return true;
    }

    public static boolean checkInitialState(Wtg wtg) {
        if (wtg.getInitialState() == null) {
            DialogUtils.showError("The WTG does not have an initial state.");
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
                    DialogUtils.showError("Signal '" + signalName + "' has inconsistent types in different waveforms.");
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
                Set<MathNode> preset = wtg.getPreset(transition);
                //Every transition has exactly one transition for "signal" in its preset
                if (!checkTransitionConnections(wtg, waveform, preset, signal, "preset")) {
                    return false;
                }
                //Every transition has exactly one transition for "signal" in its postset
                Set<MathNode> postset = wtg.getPostset(transition);
                if (!checkTransitionConnections(wtg, waveform, postset, signal, "postset")) {
                    return false;
                }
            }

        }
        return true;
    }

    private static boolean checkTransitionConnections(Wtg wtg, Waveform waveform, Set<? extends MathNode> connections,
            Signal signal, String connectionType) {
        String msg = "Transition for signal '" + wtg.getName(signal) + "' in waveform '" + wtg.getName(waveform) + "' ";
        if (connections.isEmpty()) {
            msg += "has an empty " + connectionType + ".";
            DialogUtils.showError(msg);
            return false;
        }
        boolean signalInConnection = false;
        for (MathNode node : connections) {
            Event connectedTransition = (Event) node;
            if (connectedTransition.getSignal() == signal) {
                if (signalInConnection) {
                    msg += "has more than one transition for the same signal in its " + connectionType + ".";
                    DialogUtils.showError(msg);
                    return false;
                }
                signalInConnection = true;
            }
        }
        if (!signalInConnection) {
            msg += " does not have a transition for the same signal in its " + connectionType + ".";
            DialogUtils.showError(msg);
            return false;
        }

        return true;
    }

    public static boolean checkExitEventPresetPostset(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (ExitEvent exit : wtg.getExits(waveform)) {
                String msg = "Waveform '" + wtg.getName(waveform) + "' has an exit event ";
                if (!wtg.getPostset(exit).isEmpty()) {
                    msg +=  "with a non-empty postset.";
                    DialogUtils.showError(msg);
                    return false;
                }
                Set<MathNode> preset = wtg.getPreset(exit);
                if (preset.isEmpty()) {
                    msg += "with an empty preset.";
                    DialogUtils.showError(msg);
                    return false;
                }
                if (preset.size() > 1) {
                    msg += "with more than one transition in its preset.";
                    DialogUtils.showError(msg);
                    return false;
                }
                if (((Event) preset.iterator().next()).getSignal() != exit.getSignal()) {
                    msg += "connected to an event with a different signal.";
                    DialogUtils.showError(msg);
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkEntryEventPresetPostset(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (EntryEvent entry : wtg.getEntries(waveform)) {
                String msg = "Waveform '" + wtg.getName(waveform) + "' has an entry event ";
                if (!wtg.getPreset(entry).isEmpty()) {
                    msg += "with a non-empty preset.";
                    DialogUtils.showError(msg);
                    return false;
                }
                Set<MathNode> postset = wtg.getPostset(entry);
                if (postset.isEmpty()) {
                    msg += "with an empty postset.";
                    DialogUtils.showError(msg);
                    return false;
                }
                if (postset.size() > 1) {
                    msg += "with more than one transition in its postset.";
                    DialogUtils.showError(msg);
                    return false;
                }
                if (((Event) postset.iterator().next()).getSignal() != entry.getSignal()) {
                    msg += "connected to an event with a different signal.";
                    DialogUtils.showError(msg);
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
                DialogUtils.showError("Waveform '" + wtg.getName(waveform) + "' has repeated entry events.");
                return false;
            }
            if (!checkEventsMatchSignals(wtg, waveform, entryEventSignals, "Entry")) {
                return false;
            }

            //Check if every signal has exactly one exit event, and no other exit events exist
            Set<String> exitEventSignals = getExitEventSignalNames(wtg, waveform);
            if (wtg.getExits(waveform).size() > exitEventSignals.size()) {
                DialogUtils.showError("Waveform '" + wtg.getName(waveform) + "' has repeated exit events.");
                return false;
            }
            if (!checkEventsMatchSignals(wtg, waveform, exitEventSignals, "exit")) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkEventsMatchSignals(Wtg wtg, Waveform waveform, Set<String> eventSignals,
                                                   String eventType) {
        Collection<Signal> waveformSignals = wtg.getSignals(waveform);
        if (waveformSignals.size() < eventSignals.size()) {
            DialogUtils.showError("Waveform '" + wtg.getName(waveform) +
                    "' has " + eventType + " events for unknown signals.");
            return false;
        }
        for (Signal signal : waveformSignals) {
            String signalName = wtg.getName(signal);
            if (!eventSignals.contains(signalName)) {
                DialogUtils.showError(eventType + " event missing for signal '" + signalName +
                        "' in waveform '" + wtg.getName(waveform) + "'.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkFirstTransitionIsValid(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (EntryEvent entry : wtg.getEntries(waveform)) {
                if (entry.getSignal().getType() != Signal.Type.INPUT) {
                    for (MathNode node: wtg.getPostset(entry)) {
                        if ((isFirstTransition(wtg, node)) && (isWaveformInChoice(wtg, waveform))) {
                            DialogUtils.showError("Signal '" + wtg.getName(entry.getSignal())
                                    + "' cannot be fired immediately after a choice, in waveform '"
                                    + wtg.getName(waveform) + "'.");
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

    public static boolean checkInitialValueForGuards(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (Signal signal : wtg.getSignals(waveform)) {
                String signalName = wtg.getName(signal);
                if (waveform.getGuard().containsKey(signalName)) {
                    Signal.State signalGuardState = (waveform.getGuard().get(signalName)) ?
                            Signal.State.HIGH : Signal.State.LOW;
                    if (signal.getInitialState() != signalGuardState) {
                        DialogUtils.showError("The initial state for signal '" + signalName +
                                "' in waveform '" + wtg.getName(waveform) + "' does not match the guard value.");
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
                    DialogUtils.showError("The guard from waveform '" + wtg.getName(waveform) +
                            "' is defined for signal '" + signalGuard + "', but that signal is not used in the waveform.");
                    return false;
                }
            }

        }
        return true;
    }

    public static boolean checkInitialStateHasNoGuard(Wtg wtg) {
        State initialState = wtg.getInitialState();
        for (MathNode node : wtg.getPostset(initialState)) {
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
                if (signal.getType() != Signal.Type.INPUT) {
                    String signalName = wtg.getName(signal);
                    if (waveform.getGuard().containsKey(signalName)) {
                        DialogUtils.showError("signal '" + signalName + "' cannot be in a guard.");
                        return false;
                    }
                    if ((signal.getInitialState() == Signal.State.UNSTABLE) ||
                            (signal.getInitialState() == Signal.State.STABLE)) {
                        DialogUtils.showError("Signal " + signalName +
                                " has an illegal initial state in waveform '" + wtg.getName(waveform) + "'.");
                        return false;
                    }
                }
            }

            for (TransitionEvent transition : wtg.getTransitions(waveform)) {
                TransitionEvent.Direction direction = transition.getDirection();
                if ((direction == TransitionEvent.Direction.DESTABILISE) ||
                        (direction == TransitionEvent.Direction.STABILISE)) {
                    Signal signal = transition.getSignal();
                    if (signal.getType() != Signal.Type.INPUT) {
                        DialogUtils.showError("Signal '" + wtg.getName(signal) +
                                "' has an illegal transition in waveform '" + wtg.getName(waveform) + "'.");
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
            DialogUtils.showError("There are missing waveforms in the postset of state '"
                    + wtg.getName(state) + "'.");
            return false;
        }

        Set<List<Boolean>> guardConditions = new HashSet<>();
        for (Map.Entry<String, Guard> guardEntry : guards.entrySet()) {
            Guard guard = guardEntry.getValue();
            if (guard.size() < guardedSignals.size()) {
                DialogUtils.showError("The guard from waveform '" + guardEntry.getKey() + "' is missing signals.");
                return false;
            }
            List<Boolean> signalValues = new ArrayList<>();
            for (String signalName : guardedSignals) {
                signalValues.add(guard.get(signalName));
            }
            if (guardConditions.contains(signalValues)) {
                DialogUtils.showError("The guard '" + guard.toString() + "' is repeated at state '"
                        + wtg.getName(state) + "'.");
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
                if (!wtg.getPreset(waveform).isEmpty()) {
                    MathNode precedingState = wtg.getPreset(waveform).iterator().next();
                    for (String signalGuarded : guard.keySet()) {
                        Signal.State previousState = getFinalSignalStateForSignalFromNode(wtg, precedingState,
                                signalGuarded);
                        if (previousState != Signal.State.STABLE) {
                            DialogUtils.showError("Signal '" + signalGuarded
                                    + "' should be stable before reaching the state '"
                                    + wtg.getName(precedingState) + "'.");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkReachability(Wtg wtg) {
        //Checks whether all states and waveforms are reachable from the initial state
        if (!checkNodeReachability(wtg)) {
            return false;
        }
        //Checks whether all transitions for all waveforms are reachable from the entry events
        if (!checkTransitionReachability(wtg)) {
            return false;
        }
        return true;
    }

    public static boolean checkNodeReachability(Wtg wtg) {
        Set<MathNode> reachableNodes = new HashSet<>();
        State initialState = wtg.getInitialState();
        Queue<MathNode> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(initialState);
        reachableNodes.add(initialState);
        //BFS
        while (!nodesToVisit.isEmpty()) {
            MathNode node = nodesToVisit.poll();
            for (MathNode n : wtg.getPostset(node)) {
                if (!reachableNodes.contains(n)) {
                    reachableNodes.add(n);
                    nodesToVisit.add(n);
                }
            }
        }
        //Error handling
        List<String> unreachableNodeNames = new ArrayList<>();
        for (Waveform waveform : wtg.getWaveforms()) {
            if (reachableNodes.contains(waveform)) continue;
            unreachableNodeNames.add(wtg.getName(waveform));
        }
        for (State state : wtg.getStates()) {
            if (reachableNodes.contains(state)) continue;
            unreachableNodeNames.add(wtg.getName(state));
        }
        if (!unreachableNodeNames.isEmpty()) {
            DialogUtils.showError(LogUtils.getTextWithRefs("Unreachable node", unreachableNodeNames));
            return false;
        }
        return true;
    }

    public static boolean checkTransitionReachability(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            if (!checkTransitionReachability(wtg, waveform)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkTransitionReachability(Wtg wtg, Waveform waveform) {
        Set<Event> reachableTransitions = new HashSet<>();
        int nonReachableTransitions = wtg.getTransitions(waveform).size();
        Queue<Event> eventsToVisit = new LinkedList<>();
        for (Event entryEvent : wtg.getEntries(waveform)) {
            eventsToVisit.add(entryEvent);
        }
        //BFS
        while (!eventsToVisit.isEmpty()) {
            Event event = eventsToVisit.poll();
            for (MathNode e : wtg.getPostset(event)) {
                if ((!reachableTransitions.contains(e)) && (e instanceof TransitionEvent)) {
                    TransitionEvent transition = (TransitionEvent) e;
                    reachableTransitions.add(transition);
                    eventsToVisit.add(transition);
                    nonReachableTransitions = nonReachableTransitions - 1;
                }
            }
        }

        if (nonReachableTransitions > 0) {
            DialogUtils.showError("There are unreachable transitions in waveform " + wtg.getName(waveform) + ".");
            return false;
        }
        return true;
    }

    public static boolean checkWaveformsHaveEntryState(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<MathNode> preset = wtg.getPreset(waveform);
            String name = wtg.getName(waveform);
            if (preset.isEmpty()) {
                DialogUtils.showError("Waveform '" + name + "' does not have an entry state.");
                return false;
            }
            if (preset.size() > 1) {
                DialogUtils.showError("Waveform '" + name + "' has several entry states.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkWaveformsHaveExitState(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<MathNode> postset = wtg.getPostset(waveform);
            String name = wtg.getName(waveform);
            if (postset.isEmpty()) {
                DialogUtils.showError("Waveform '" + name + "' does not have an exit state.");
                return false;
            }
            if (postset.size() > 1) {
                DialogUtils.showError("Waveform '" + name + "' has several exit states.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkWaveformsOnlyConnectToStates(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<MathNode> preset = wtg.getPreset(waveform);
            Set<MathNode> postset = wtg.getPostset(waveform);
            String msg = "Waveform '" + wtg.getName(waveform) + "' is connected to a non-state node.";
            for (MathNode node : preset) {
                if (!(node instanceof State)) {
                    DialogUtils.showError(msg);
                    return false;
                }
            }
            for (MathNode node : postset) {
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
                DialogUtils.showError("Waveform '" + wtg.getName(waveform) + "' is empty.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkValidInitialSignalStates(Wtg wtg) {
        for (Signal.State signalState : getInitialSignalStates(wtg).values()) {
            if (signalState == Signal.State.STABLE || signalState == Signal.State.UNSTABLE) {
                DialogUtils.showError("The initial state for a signal in a WTG can not be " +
                        signalState.toString() + ".");
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
                    for (MathNode node : wtg.getPostset(transition)) {
                        if (node instanceof TransitionEvent) {
                            TransitionEvent dstTransition = (TransitionEvent) node;
                            Signal signal = dstTransition.getSignal();
                            if (signal.getType() != Signal.Type.INPUT) {
                                String transitionName = wtg.getName(transition.getSignal()) + direction.getSymbol();
                                String signalName = wtg.getName(signal);
                                String msg = "Transition '" + transitionName + "' triggers the signal '" + signalName +
                                        "'. Stabilising/destabilising transitions should only trigger inputs.";
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
        Queue<MathNode> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(initialState);
        signalStates.put(initialState, initialSignalStates);
        while (!nodesToVisit.isEmpty()) {
            MathNode node = nodesToVisit.poll();

            if (node instanceof State) {
                nodesToVisit.addAll(wtg.getPostset(node));
            } else if (node instanceof Waveform) {
                Waveform waveform = (Waveform) node;
                MathNode predecessorNode = wtg.getPreset(waveform).iterator().next();
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
                MathNode successorNode = wtg.getPostset(waveform).iterator().next();
                if (successorNode instanceof State) {
                    if (signalStates.containsKey(successorNode)) {
                        if (!signalStates.get(successorNode).equals(finalWaveformSignalState)) {
                            String msg = "In waveform '" + wtg.getName(waveform) +
                                    "' the following signals have an inconsistent exit state with respect to the exit state of other waveforms:";
                            for (Map.Entry<String, Signal.State> signal : signalStates.get(successorNode).entrySet()) {
                                if ((finalWaveformSignalState.containsKey(signal.getKey())) &&
                                        (!finalWaveformSignalState.get(signal.getKey()).equals(signal.getValue()))) {
                                    msg = msg + " " + signal.getKey();
                                }
                            }
                            DialogUtils.showError(msg);
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
                        DialogUtils.showError("Initial signal state for signal '" + signalName +
                                "' is inconsistent with its guard in waveform '" + wtg.getName(waveform) + "'");
                        return false;
                    }
                } else {
                    DialogUtils.showError("Initial signal state for signal '" + signalName +
                            "' is inconsistent in waveform '" + wtg.getName(waveform) + "'");
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
            boolean consistent = true;
            if (direction == TransitionEvent.Direction.RISE) {
                if ((previousState != Signal.State.LOW) && (previousState != Signal.State.UNSTABLE)) {
                    consistent = false;
                }
            } else if (direction == TransitionEvent.Direction.FALL) {
                if ((previousState != Signal.State.HIGH) && (previousState != Signal.State.UNSTABLE)) {
                    consistent = false;
                }
            }  else if (direction == TransitionEvent.Direction.STABILISE) {
                if (previousState != Signal.State.UNSTABLE) {
                    consistent = false;
                }
            } else if (direction == TransitionEvent.Direction.DESTABILISE) {
                if ((previousState != Signal.State.HIGH) && (previousState != Signal.State.LOW)) {
                    consistent = false;
                }
            }
            if (!consistent) {
                DialogUtils.showError(" In waveform '" + wtg.getName(waveform) + "', inconsistent transition for signal '"
                        + wtg.getName(transition.getSignal()) + "'");
            }
        }
        return true;
    }

    public static boolean checkInputProperness(Wtg wtg) {
        for (Waveform waveform : wtg.getWaveforms()) {
            for (TransitionEvent transition : wtg.getTransitions(waveform)) {
                if (transition.getSignal().getType() == Signal.Type.INTERNAL) {
                    for (MathNode node : wtg.getPostset(transition)) {
                        if (node instanceof TransitionEvent) {
                            TransitionEvent dstTransition = (TransitionEvent) node;
                            if (dstTransition.getSignal().getType() == Signal.Type.INPUT) {
                                String msg = "Internal signal '" + wtg.getName(transition.getSignal())
                                        + "' triggers input signal '"
                                        + wtg.getName(dstTransition.getSignal()) + "'.";
                                DialogUtils.showError(msg);
                                return false;
                            }
                        } else if (node instanceof ExitEvent) {
                            for (MathNode predecesor : wtg.getPreset(node)) {
                                if (isLastTransition(wtg, predecesor) &&
                                        isFirstTransitionInputInSuccessorWaveforms(wtg, waveform)) {
                                    DialogUtils.showError("Internal signal '" + wtg.getName(transition.getSignal())
                                            + "' triggers an input signal with its transition at the end of waveform '"
                                            + wtg.getName(waveform) + "'.");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean isFirstTransitionInputInSuccessorWaveforms(Wtg wtg, Waveform waveform) {
        for (MathNode successorState : wtg.getPostset(waveform)) {
            for (MathNode successorWaveform : wtg.getPostset(successorState)) {
                if (firstTransitionInput(wtg, (Waveform) successorWaveform)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean firstTransitionInput(Wtg wtg, Waveform waveform) {
        for (EntryEvent entry : wtg.getEntries(waveform)) {
            if (entry.getSignal().getType() == Signal.Type.INPUT) {
                for (MathNode node: wtg.getPostset(entry)) {
                    if (isFirstTransition(wtg, node)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isLastTransition(Wtg wtg, MathNode node) {
        if (!(node instanceof EntryEvent)) {
            if (wtg.getPostset(node).size() == 1) {
                return wtg.getPostset(node).iterator().next() instanceof ExitEvent;
            }
        }
        return false;
    }

    private static boolean isFirstTransition(Wtg wtg, MathNode node) {
        if (!(node instanceof ExitEvent)) {
            return wtg.getPreset(node).size() == 1;
        }
        return false;
    }
}
