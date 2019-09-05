package org.workcraft.plugins.xbm;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fsm.VisualEvent;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

public class Burst extends Symbol {

    public enum Direction {

        PLUS("+"),
        MINUS("-"),
        STABLE("~"),
        UNSTABLE("*");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public Direction toggle() {
            switch(this) {
                case MINUS:
                    return Direction.PLUS;
                case PLUS:
                    return Direction.MINUS;
                case STABLE:
                    return Direction.UNSTABLE;
                case UNSTABLE:
                    return Direction.STABLE;
                default:
                    return this;
            }
        }

        public static Direction convertFromString(String value) {
            if (value.equals(PLUS.toString())) {
                return PLUS;
            }
            else if (value.equals(MINUS.toString())) {
                return MINUS;
            }
            else if (value.equals(STABLE.toString())) {
                return STABLE;
            }
            else if (value.equals(UNSTABLE.toString())) {
                return UNSTABLE;
            }
            else throw new ArgumentException("An unknown direction was set for the signal.");
        }
    }

    private Map<Signal, Direction> direction = new LinkedHashMap<>();
    private XbmState from;
    private XbmState to;

    public Burst() {
    }

    public Burst(XbmState from, XbmState to) {
        this.from = from;
        this.to = to;
        for (Signal s: this.from.getSignals()) {

            if (from.getEncoding().get(s) == SignalState.HIGH && to.getEncoding().get(s) == SignalState.LOW) direction.put(s, Direction.MINUS);
            else if (from.getEncoding().get(s) == SignalState.LOW && to.getEncoding().get(s) == SignalState.HIGH) direction.put(s, Direction.PLUS);
            else if (from.getEncoding().get(s) == SignalState.DDC) direction.put(s, Direction.UNSTABLE);
        }
    }

    public Map<Signal, Direction> getDirection() {
        return direction;
    }

    public Set<Signal> getSignals() {
        return direction.keySet();
    }

    public Set<Signal> getSignals(Signal.Type type) {
        Set<Signal> result = new HashSet<>();
        for (Signal s: getSignals()) {
            if (s.getType() == type) {
                result.add(s);
            }
        }
        return result;
    }

    public XbmState getFrom() {
        return from;
    }

    public XbmState getTo() {
        return to;
    }

    public void addOrChangeSignalDirection(Signal s, Direction d) {
        direction.put(s, d);
    }

    public void addOrChangeSignalDirection(Signal s, SignalState fromState, SignalState toState) {

        if (fromState == SignalState.LOW && toState == SignalState.HIGH) {
            direction.put(s, Direction.PLUS);
        }
        else if (fromState == SignalState.HIGH && toState == SignalState.LOW) {
            direction.put(s, Direction.MINUS);
        }
        else if ((fromState == SignalState.DDC && toState == SignalState.HIGH)) {
            direction.put(s, Direction.PLUS);
        }
        else if ((fromState == SignalState.DDC && toState == SignalState.LOW)) {
            direction.put(s, Direction.MINUS);
        }
        else if ((fromState != SignalState.DDC && toState == SignalState.DDC)) {
            direction.put(s, Direction.UNSTABLE);
        }
        else if ((fromState == SignalState.HIGH && toState == SignalState.HIGH) ||
                 (fromState == SignalState.LOW && toState == SignalState.LOW) ||
                 (fromState == SignalState.DDC && toState == SignalState.DDC)) {
            direction.remove(s);
        }
    }

    public void setFrom(XbmState from) {
        this.from = from;
    }

    public void setTo(XbmState to) {
        this.to = to;
    }

    public void removeSignal(Signal s) {
        if (direction.containsKey(s)) direction.remove(s);
    }

    public boolean containsDirectedDontCare() {
        for (Map.Entry<Signal, Direction> entry: direction.entrySet()) {
            if (entry.getValue() == Direction.UNSTABLE) {
                return true;
            }
        }
        return false;
    }

    public String getAsString() {

        final Map<Signal, Direction> targetSigs = new LinkedHashMap<>();
        targetSigs.putAll(direction);

        String inputs = "", outputs = "";

        for (Map.Entry<Signal, Burst.Direction> entry: targetSigs.entrySet()) {
            Signal signal = entry.getKey();
            Burst.Direction direction = entry.getValue();

            String signalName = signal.getName();
            switch (signal.getType()) {
                case INPUT:
                    inputs = appendTargetToList(inputs, signalName + direction);
                    break;
                case OUTPUT:
                    outputs = appendTargetToList(outputs, signalName + direction);
                    break;
                case DUMMY: case CONDITIONAL:
                    break;
                default:
                    throw new RuntimeException("An unknown signal type was detected for signal " + signalName);
            }
        }
        if (!inputs.isEmpty() || !outputs.isEmpty()) return inputs + " / " + outputs;
        else return "" + VisualEvent.EPSILON_SYMBOL;
    }

    private final static String appendTargetToList(final String list, final String target) {
        String newList = list;
        if (!list.isEmpty()) newList += ", ";
        newList += target;
        return newList;
    }
}
