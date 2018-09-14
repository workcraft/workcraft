package org.workcraft.plugins.wtg.commands;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.plugins.dtd.ExitEvent;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.TransitionEvent;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.plugins.wtg.State;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.util.DialogUtils;

import java.util.*;

public class WtgVerificationUtils {


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
                DialogUtils.showWarning("Waveforms should have at least one transition.");
                return false;
            }
        }
        return true;
    }

    public static boolean checkValidInitialStates(Wtg wtg) {
        for (Signal.State signalState : wtg.getInitialSignalStates().values()) {
            if (signalState == Signal.State.STABLE) {
                DialogUtils.showError("The initial state for a signal in a WTG can not be " +
                        Signal.State.STABLE.toString());
                return false;
            }
        }
        return true;
    }

    public static boolean checkConsistency(Wtg wtg) {
        Map<String, Signal.State> initialSignalStates = wtg.getInitialSignalStates();
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
                finalWaveformSignalState.putAll(getFinalSignalStateFromWaveform(wtg, waveform));
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

    private static Map<String, Signal.State> getFinalSignalStateFromWaveform(Wtg wtg, Waveform waveform) {
        Map<String, Signal.State> result = new HashMap<>();
        for (ExitEvent exit : wtg.getExits(waveform)) {
            String signalName = wtg.getName(exit.getSignal());
            Signal.State previousState = wtg.getPreviousState(exit);
            result.put(signalName, previousState);
        }
        // FIXME: The next section of code is added in case a waveform has a guard for a signal, but does not use said signal.
        // FIXME: If we later decide that this is illegal, this code can be removed, else, this comment will go away.
        Guard guard = waveform.getGuard();
        for (String signalGuarded : guard.keySet()) {
            if (!result.containsKey(signalGuarded)) {
                Signal.State guardValue = (guard.get(signalGuarded)) ? Signal.State.HIGH : Signal.State.LOW;
                result.put(signalGuarded, guardValue);
            }
        }
        return result;
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
                                DialogUtils.showWarning(msg);
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

}
