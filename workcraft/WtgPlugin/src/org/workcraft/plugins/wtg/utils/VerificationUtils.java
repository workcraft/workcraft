package org.workcraft.plugins.wtg.utils;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TextUtils;

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
//        if (!checkGuardFunctions(wtg)) {
//            return false;
//        }
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
        return checkUnstableSignalTriggers(wtg);
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
            if (waveform.getGuard().isEmpty()) {
                for (EntryEvent entry : wtg.getEntries(waveform)) {
                    if (entry.getSignal().getType() != Signal.Type.INPUT) {
                        for (MathNode node : wtg.getPostset(entry)) {
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
            if (node instanceof Waveform waveform) {
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
                DialogUtils.showError("The guard '" + guard + "' is repeated at state '"
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
        Queue<MathNode> nodesToVisit = new ArrayDeque<>();
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
            DialogUtils.showError(TextUtils.wrapMessageWithItems("Unreachable node", unreachableNodeNames));
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
        Queue<Event> eventsToVisit = new ArrayDeque<>(wtg.getEntries(waveform));
        //BFS
        while (!eventsToVisit.isEmpty()) {
            Event event = eventsToVisit.poll();
            for (MathNode e : wtg.getPostset(event)) {
                if ((!reachableTransitions.contains(e)) && (e instanceof TransitionEvent transition)) {
                    reachableTransitions.add(transition);
                    eventsToVisit.add(transition);
                    nonReachableTransitions -= 1;
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
            String msg = "Waveform '" + wtg.getName(waveform) + "' is connected to a non-state node.";
            Set<MathNode> preset = wtg.getPreset(waveform);
            for (MathNode node : preset) {
                if (!(node instanceof State)) {
                    DialogUtils.showError(msg);
                    return false;
                }
            }
            Set<MathNode> postset = wtg.getPostset(waveform);
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
                DialogUtils.showError("The initial state for a signal in a WTG can not be " + signalState + ".");
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
                        if (node instanceof TransitionEvent dstTransition) {
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
        Queue<MathNode> nodesToVisit = new ArrayDeque<>();
        nodesToVisit.add(initialState);
        signalStates.put(initialState, initialSignalStates);
        while (!nodesToVisit.isEmpty()) {
            MathNode node = nodesToVisit.poll();

            if (node instanceof State) {
                nodesToVisit.addAll(wtg.getPostset(node));
            } else if (node instanceof Waveform waveform) {
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
                            StringBuilder msg = new StringBuilder("In waveform '" + wtg.getName(waveform) +
                                    "' the following signals have an inconsistent exit state with respect to the exit state of other waveforms:");
                            for (Map.Entry<String, Signal.State> signal : signalStates.get(successorNode).entrySet()) {
                                if ((finalWaveformSignalState.containsKey(signal.getKey())) &&
                                        (finalWaveformSignalState.get(signal.getKey()) != signal.getValue())) {
                                    msg.append(' ');
                                    msg.append(signal.getKey());
                                }
                            }
                            DialogUtils.showError(msg.toString());
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
                        if (node instanceof TransitionEvent dstTransition) {
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

    public static boolean synthesisGuidelines(Wtg wtg) {
        Map<Waveform, Set<String>> firedBeforeOut = new HashMap<>();
        Map<Waveform, Set<String>> firedAfterOut = new HashMap<>();
        Map<Waveform, Boolean> hasOutput = new HashMap<>();
        for (Waveform waveform : wtg.getWaveforms()) {
            Set<String> firedBefore = new HashSet<>();
            Set<String> firedAfter = new HashSet<>();
            if (!waveformFollowsSynthesisGuidelines(wtg, waveform, firedBefore, firedAfter)) {
                return false;
            }
            firedBeforeOut.put(waveform, firedBefore);
            firedAfterOut.put(waveform, firedAfter);
            hasOutput.put(waveform, hasOutputTransition(wtg, waveform));
        }

        Set<String> inputSignals = new HashSet<>();
        for (Signal signal : wtg.getSignals()) {
            if (signal.getType() == Signal.Type.INPUT) {
                inputSignals.add(wtg.getName(signal));
            }
        }

        for (String signal : inputSignals) {
            if (!signalFollowsSynthesisGuidelines(wtg, signal, firedBeforeOut, firedAfterOut, hasOutput)) {
                return false;
            }
        }

        return true;
    }

    private static boolean signalFollowsSynthesisGuidelines(Wtg wtg, String signal, Map<Waveform, Set<String>> firedBeforeOut,
                                                            Map<Waveform, Set<String>> firedAfterOut, Map<Waveform, Boolean> hasOutput) {
        Queue<Waveform> toVisit = new ArrayDeque<>();
        Set<Waveform> visited = new HashSet<>();
        //We only need to look waveforms in which the signal fired after any output
        for (Map.Entry<Waveform, Set<String>> entry : firedAfterOut.entrySet()) {
            if (entry.getValue().contains(signal)) {
                toVisit.add(entry.getKey());
                visited.add(entry.getKey());
            }
        }

        while (!toVisit.isEmpty()) {
            Waveform visitingWaveform = toVisit.poll();

            State followingState;
            if ((wtg.getPostset(visitingWaveform).iterator().hasNext()) &&
                    (wtg.getPostset(visitingWaveform).iterator().next() instanceof State)) {
                followingState = (State) wtg.getPostset(visitingWaveform).iterator().next();
            } else {
                continue;
            }

            for (MathNode node : wtg.getPostset(followingState)) {
                if (!(node instanceof Waveform nextWaveform)) {
                    continue;
                }

                if (firedBeforeOut.get(nextWaveform).contains(signal)) {
                    DialogUtils.showError("Input signal '" + signal + "' can fire before reaching waveform '" +
                            wtg.getName(nextWaveform) +
                            "' without an output in between, yet it can fire in that waveform before any output does.");
                    return false;
                }

                //If a succeeding waveform does not have any output, we have to look at its succeeding waveforms too
                if ((!hasOutput.get(nextWaveform)) && (!visited.contains(nextWaveform))) {
                    toVisit.add(nextWaveform);
                    visited.add(nextWaveform);
                }
            }
        }
        return true;
    }

    private static Map<TransitionEvent, Integer> indexInputTransitionsBySequence(Wtg wtg, Waveform waveform) {
        Map<TransitionEvent, Integer> result = new HashMap<>();
        //sequential index for every input transition. When comparing transitions of the same signal,
        //a smaller index implies the transition fired before.
        for (EntryEvent entry : wtg.getEntries(waveform)) {
            Integer idx = 1;
            Signal signal = entry.getSignal();
            if (signal.getType() != Signal.Type.INPUT) {
                continue;
            }
            MathNode nextEvent = entry;
            boolean transitionFound = true;
            while (transitionFound) {
                transitionFound = false;
                for (MathNode node : wtg.getPostset(nextEvent)) {
                    if ((node instanceof TransitionEvent) && (((TransitionEvent) node).getSignal() == signal)) {
                        result.put((TransitionEvent) node, idx);
                        idx++;
                        nextEvent = node;
                        transitionFound = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static List<Signal> getGuardedSignals(Wtg wtg, Waveform waveform) {
        List<Signal> result = new LinkedList<>();
        for (Signal signal : wtg.getSignals(waveform)) {
            String signalName = wtg.getName(signal);
            if (waveform.getGuard().containsKey(signalName)) {
                result.add(signal);
            }
        }
        return result;
    }

    private static boolean checkInputFiredTwice(TransitionEvent visitingTransition,
                                                Map<TransitionEvent, Map<Signal, Pair<Boolean, Integer>>> inputsFiredAfterOutput) {
        Signal transitionSignal = visitingTransition.getSignal();
        if ((transitionSignal.getType() == Signal.Type.INPUT) &&
                (visitingTransition.getDirection() != TransitionEvent.Direction.STABILISE) &&
                (inputsFiredAfterOutput.get(visitingTransition).containsKey(transitionSignal))) {
            return inputsFiredAfterOutput.get(visitingTransition).get(transitionSignal).getFirst();
        }
        return false;
    }

    private static void processTransition(Wtg wtg, TransitionEvent visitingTransition, Set<TransitionEvent> outputHasFired,
                                          Set<String> firedBeforeOut, Integer index,
                                          Map<TransitionEvent, Map<Signal, Pair<Boolean, Integer>>> inputsFiredAfterOutput) {
        Signal transitionSignal = visitingTransition.getSignal();
        if (transitionSignal.getType() == Signal.Type.INPUT) {
            if (visitingTransition.getDirection() != TransitionEvent.Direction.STABILISE) {
                //Stabilise transitions can always fire even after another transition (assuming consistency).
                //Any other transition has to be checked

                if (!outputHasFired.contains(visitingTransition)) {
                    //if no output has been fired yet and this is an input, then signal fired first
                    firedBeforeOut.add(wtg.getName(transitionSignal));
                }

                boolean needsOutputFiring = visitingTransition.getDirection() != TransitionEvent.Direction.DESTABILISE;
                //destabilise transitions allow to fire again before any output

                inputsFiredAfterOutput.get(visitingTransition).put(transitionSignal, new Pair<>(needsOutputFiring, index));
            }
        } else {
            //Transitions for output/internal allow all the input signals to fire again
            outputHasFired.add(visitingTransition);
            for (Signal inputsFired : inputsFiredAfterOutput.get(visitingTransition).keySet()) {
                Integer firedTransitionIdx = inputsFiredAfterOutput.get(visitingTransition).get(inputsFired).getSecond();
                inputsFiredAfterOutput.get(visitingTransition).put(inputsFired, new Pair<>(false, firedTransitionIdx));
            }
        }
    }

    private static void mergeSignalStatus(TransitionEvent visitingTransition, TransitionEvent nextEvent,
                                          Map<TransitionEvent, Map<Signal, Pair<Boolean, Integer>>> inputsFiredAfterOutput) {
        if (inputsFiredAfterOutput.get(nextEvent).isEmpty()) {
            //This is the first time we visit nextEvent transition. It first inherits all the signal status
            //from visitingTransition
            inputsFiredAfterOutput.get(nextEvent).putAll(inputsFiredAfterOutput.get(visitingTransition));
        } else {
            //The nextEvent transition was previously initialized. It means that nextEvent has more than 1
            //dependence. We have to combine the signal status with the visitingTransition
            for (Map.Entry<Signal, Pair<Boolean, Integer>> visitingInput:
                    inputsFiredAfterOutput.get(visitingTransition).entrySet()) {
                Signal input = visitingInput.getKey();
                Pair<Boolean, Integer> visitingEventInput = visitingInput.getValue();

                if (inputsFiredAfterOutput.get(nextEvent).containsKey(input)) {
                    //if the signal is known by both transitions...
                    Pair<Boolean, Integer> nextEventInput = inputsFiredAfterOutput.get(nextEvent).get(input);
                    if (nextEventInput.getFirst() != visitingEventInput.getFirst()) {
                        //if there has been an output in one of the paths, but not in all of them...
                        if (visitingEventInput.getSecond().equals(nextEventInput.getSecond())) {
                            //if the last transition is the same common ancestor, and there has been an output
                            //in one of the paths, we know that an output must fire before reaching "nextEvent"
                            inputsFiredAfterOutput.get(nextEvent).put(input,
                                    new Pair<>(false, nextEventInput.getSecond()));
                        } else if (visitingEventInput.getSecond() > nextEventInput.getSecond()) {
                            //If the last transition from visitingEvent is younger than the one in
                            //nextEvent, then we propagate the one from visitingEvent
                            inputsFiredAfterOutput.get(nextEvent).put(input, visitingEventInput);
                        }
                        //Else, nextEvent carries the correct signal status
                    } else {
                        //If all paths had an output or none had, then nextEvent has the correct boolean value.
                        //The transition index must be set to the younger transition
                        Integer youngerTransition = Math.max(visitingEventInput.getSecond(),
                                nextEventInput.getSecond());
                        inputsFiredAfterOutput.get(nextEvent).put(input, new Pair<>(nextEventInput.getFirst(),
                                youngerTransition));
                    }
                } else {
                    //if the signal is only know in visitingTransition, nextEvent inherits its status
                    inputsFiredAfterOutput.get(nextEvent).put(input, visitingEventInput);
                }
            }
        }
    }

    private static boolean waveformFollowsSynthesisGuidelines(Wtg wtg, Waveform waveform, Set<String> firedBeforeOut,
                                                              Set<String> firedAfterOut) {
        //firedBeforeOut will contain the input signals that fire before any output does
        //firedAfterOut will contain the input signals that fire after any output does
        Map<TransitionEvent, Integer> transitionDependencies = new HashMap<>();
        for (TransitionEvent transition : wtg.getTransitions(waveform)) {
            transitionDependencies.put(transition, wtg.getPreset(transition).size());
        }

        Queue<TransitionEvent> toVisit = new ArrayDeque<>();

        Set<TransitionEvent> outputHasFired = new HashSet<>();
        Map<TransitionEvent, Map<Signal, Pair<Boolean, Integer>>> inputsFiredAfterOutput = new HashMap<>();
        //Contains, for each transition, which is the last fired transition (as an integer index) and whether there has
        //been any output fired after that last fired transition (true == input cannot fire again, i.e. no output fired)

        for (TransitionEvent transition : wtg.getTransitions(waveform)) {
            inputsFiredAfterOutput.put(transition, new HashMap<>());
        }
        TransitionEvent finalEvent = new TransitionEvent(); //"ghost" event that fires any other transition
        inputsFiredAfterOutput.put(finalEvent, new HashMap<>());

        Map<TransitionEvent, Integer> transitionIndex = indexInputTransitionsBySequence(wtg, waveform);

        List<Signal> guardedSignals = getGuardedSignals(wtg, waveform);
        for (Signal signal : guardedSignals) {
            firedBeforeOut.add(wtg.getName(signal));
        }

        for (EntryEvent entry : wtg.getEntries(waveform)) {
            for (MathNode node : wtg.getPostset(entry)) {
                if (node instanceof TransitionEvent transition) {
                    Integer dependencies = transitionDependencies.computeIfPresent(transition, (k, v) -> v - 1);
                    if (dependencies == 0) {
                        toVisit.add(transition);

                        //Guards are considered transitions that fire before any other transition for the guarded signal
                        for (Signal signal : guardedSignals)  {
                            inputsFiredAfterOutput.get(transition).put(signal, new Pair<>(true, 0));
                        }
                    }
                }
            }
        }

        //We traverse the dependency tree with a BFS
        while (!toVisit.isEmpty()) {
            TransitionEvent visitingTransition = toVisit.poll();

            if (checkInputFiredTwice(visitingTransition, inputsFiredAfterOutput)) {
                DialogUtils.showError("In waveform '" + wtg.getName(waveform) + "' input signal '" +
                        wtg.getName(visitingTransition.getSignal()) + "' may fire two times without an output in between.");
            }

            processTransition(wtg, visitingTransition, outputHasFired, firedBeforeOut,
                    transitionIndex.get(visitingTransition), inputsFiredAfterOutput);

            for (MathNode node : wtg.getPostset(visitingTransition)) {
                MathNode event = node;
                if ((event instanceof ExitEvent) && (wtg.getPostset(visitingTransition).size() == 1)) {
                    event = finalEvent; //we set the event to the "ghost" final event
                }

                if (event instanceof TransitionEvent nextEvent) {
                    if (outputHasFired.contains(visitingTransition)) {
                        //an output has fired in this or in past transitions
                        outputHasFired.add(nextEvent);
                    }

                    mergeSignalStatus(visitingTransition, nextEvent, inputsFiredAfterOutput);

                    if (nextEvent == finalEvent) {
                        continue;
                    }
                    Integer dependencies = transitionDependencies.computeIfPresent(nextEvent, (k, v) -> v - 1);
                    if (dependencies == 0) {
                        toVisit.add(nextEvent);
                    }
                }
            }
        }

        //Finally, we can know the input signals that may fire after any output by looking at the "ghost" final event
        for (Map.Entry<Signal, Pair<Boolean, Integer>> finalTrans : inputsFiredAfterOutput.get(finalEvent).entrySet()) {
            if (finalTrans.getValue().getFirst()) {
                firedAfterOut.add(wtg.getName(finalTrans.getKey()));
            }
        }
        return true;
    }

    private static boolean hasOutputTransition(Wtg wtg, Waveform waveform) {
        for (TransitionEvent transition : wtg.getTransitions(waveform)) {
            if (transition.getSignal().getType() != Signal.Type.INPUT) {
                return true;
            }
        }
        return false;
    }

}
