package org.workcraft.plugins.wtg.utils;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.dtd.EntryEvent;
import org.workcraft.plugins.dtd.ExitEvent;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.TransitionEvent;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;

import java.util.*;

public class WtgUtils {

    public static Set<String> getUnstableSignalNames(Wtg wtg) {
        Set<String> result = new HashSet<>();
        for (Waveform waveform : wtg.getWaveforms()) {
            for (TransitionEvent srcTransition : wtg.getTransitions(waveform)) {
                TransitionEvent.Direction direction = srcTransition.getDirection();
                if ((direction == TransitionEvent.Direction.DESTABILISE) ||
                        (direction == TransitionEvent.Direction.STABILISE)) {
                    Signal signal = srcTransition.getSignal();
                    String signalName = wtg.getName(signal);
                    result.add(signalName);
                }
            }

            for (Signal signal : wtg.getSignals(waveform)) {
                if (signal.getInitialState() == Signal.State.UNSTABLE) {
                    result.add(wtg.getName(signal));
                }
            }
        }
        return result;
    }

    public static Set<String> getEntryEventSignalNames(Wtg wtg, Waveform waveform) {
        Set<String> result = new HashSet<>();
        for (EntryEvent entry : wtg.getEntries(waveform)) {
            result.add(wtg.getName(entry.getSignal()));
            String signalName = wtg.getName(entry.getSignal());
            result.add(signalName);
        }
        return result;
    }

    public static Set<String> getExitEventSignalNames(Wtg wtg, Waveform waveform) {
        Set<String> result = new HashSet<>();
        for (ExitEvent exit : wtg.getExits(waveform)) {
            result.add(wtg.getName(exit.getSignal()));
            String signalName = wtg.getName(exit.getSignal());
            result.add(signalName);
        }
        return result;
    }

    public static Map<String, Guard> getGuardFromState(Wtg wtg, State state) {
        Map<String, Guard> result = new HashMap<>();
        for (MathNode node : wtg.getPostset(state)) {
            if (node instanceof Waveform) {
                result.put(wtg.getName(node), ((Waveform) node).getGuard());
            }
        }
        return result;
    }

    public static Map<String, Signal.State> getFinalSignalStatesFromWaveform(Wtg wtg, Waveform waveform) {
        Map<String, Signal.State> result = new HashMap<>();
        for (ExitEvent exit : wtg.getExits(waveform)) {
            String signalName = wtg.getName(exit.getSignal());
            Signal.State previousState = wtg.getPreviousState(exit);
            result.put(signalName, previousState);
        }
        return result;
    }

    public static Map<String, Signal.State> getInitialSignalStates(Wtg wtg) {
        Map<String, Signal.State> result = new HashMap<>();

        //BFS initialization
        int remainingSignals = wtg.getSignalNames().size();
        State initialState = wtg.getInitialState();
        Set<MathNode> visitedNodes = new HashSet<>();
        Queue<MathNode> nodesToVisit = new ArrayDeque<>();
        nodesToVisit.add(initialState);
        visitedNodes.add(initialState);
        //BFS main loop
        while ((!nodesToVisit.isEmpty()) && (remainingSignals > 0)) {
            MathNode node = nodesToVisit.poll();

            if (node instanceof Waveform waveform) {
                for (Signal signal : wtg.getSignals(waveform)) {
                    String signalName = wtg.getName(signal);
                    if (!result.containsKey(signalName)) {
                        result.put(signalName, signal.getInitialState());
                        remainingSignals -= 1;
                    }
                }
            }

            for (MathNode n : wtg.getPostset(node)) {
                if (!visitedNodes.contains(n)) {
                    nodesToVisit.add(n);
                    visitedNodes.add(n);
                }
            }
        }
        return result;
    }

    public static Signal.State getFinalSignalStateForSignalFromNode(Wtg wtg, MathNode node, String signalName) {
        //Returns the final signal state for a signal in a waveform or state.
        //The search is propagated backwards until the first instance of the signal is found

        Set<MathNode> visitedNodes = new HashSet<>();
        Queue<MathNode> nodesToVisit = new ArrayDeque<>();
        visitedNodes.add(node);
        nodesToVisit.add(node);
        while (!nodesToVisit.isEmpty()) {
            MathNode visitingNode = nodesToVisit.poll();
            if (visitingNode instanceof Waveform predecesorWaveform) {
                Map<String, Signal.State> finalSignalStates = getFinalSignalStatesFromWaveform(wtg, predecesorWaveform);
                if (finalSignalStates.containsKey(signalName)) {
                    return finalSignalStates.get(signalName);
                }
            }

            for (MathNode n : wtg.getPreset(visitingNode)) {
                if (!visitedNodes.contains(n)) {
                    nodesToVisit.add(n);
                    visitedNodes.add(n);
                }
            }
        }
        return null;
    }

    public static void renameSignal(Wtg wtg, String oldName, String newName) {
        if ((oldName == null) || (newName == null) || oldName.equals(newName)) {
            return;
        }
        for (Signal signal : wtg.getSignals()) {
            if (!oldName.equals(wtg.getName(signal))) continue;
            wtg.setName(signal, newName);
            signal.sendNotification(new PropertyChangedEvent(signal, Signal.PROPERTY_NAME));
        }
        for (Waveform waveform : wtg.getWaveforms()) {
            Guard guard = waveform.getGuard();
            if (!guard.containsKey(oldName)) continue;
            Guard newGuard = new Guard();
            for (Map.Entry<String, Boolean> entry : guard.entrySet()) {
                String key = oldName.equals(entry.getKey()) ? newName : entry.getKey();
                newGuard.put(key, entry.getValue());
            }
            waveform.setGuard(newGuard);
        }
    }

}
