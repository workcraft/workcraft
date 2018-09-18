package org.workcraft.plugins.wtg.commands;

import org.workcraft.dom.Node;
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
        for (Node node : wtg.getPostset(state)) {
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
        Set<Node> visitedNodes = new HashSet<>();
        Queue<Node> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(initialState);
        visitedNodes.add(initialState);
        //BFS main loop
        while ((!nodesToVisit.isEmpty()) && (remainingSignals > 0)) {
            Node node = nodesToVisit.poll();

            if (node instanceof Waveform) {
                Waveform waveform = (Waveform) node;
                for (Signal signal : wtg.getSignals(waveform)) {
                    String signalName = wtg.getName(signal);
                    if (!result.containsKey(signalName)) {
                        result.put(signalName, signal.getInitialState());
                        remainingSignals = remainingSignals - 1;
                    }
                }
            }

            for (Node n : wtg.getPostset(node)) {
                if (!visitedNodes.contains(n)) {
                    nodesToVisit.add(n);
                    visitedNodes.add(n);
                }
            }
        }
        return result;
    }
}
