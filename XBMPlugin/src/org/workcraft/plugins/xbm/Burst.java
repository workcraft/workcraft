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
        UNSTABLE("*"),
        CLEAR("CLEAR");

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
                case CLEAR:
                    return Direction.CLEAR;
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
            else if (value.equals(CLEAR.toString())) {
                return CLEAR;
            }
            else throw new ArgumentException("An unknown direction was set for the signal.");
        }
    }

    public static final String PROPERTY_DIRECTION = "Direction";

    private final Map<XbmSignal, Direction> direction = new LinkedHashMap<>();
    private XbmState from;
    private XbmState to;

    public Burst() {
    }

    public Burst(XbmState from, XbmState to) {
        this.from = from;
        this.to = to;
        for (XbmSignal s: this.from.getSignals()) {

            if (from.getEncoding().get(s) == SignalState.HIGH && to.getEncoding().get(s) == SignalState.LOW) direction.put(s, Direction.MINUS);
            else if (from.getEncoding().get(s) == SignalState.LOW && to.getEncoding().get(s) == SignalState.HIGH) direction.put(s, Direction.PLUS);
            else if (from.getEncoding().get(s) == SignalState.DDC) direction.put(s, Direction.UNSTABLE);
        }
    }

    public Map<XbmSignal, Direction> getDirection() {
        return direction;
    }

    public Set<XbmSignal> getSignals() {
        return direction.keySet();
    }

    public Set<XbmSignal> getSignals(XbmSignal.Type type) {
        Set<XbmSignal> result = new HashSet<>();
        for (XbmSignal s: getSignals()) {
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

    public void addOrChangeSignalDirection(XbmSignal s, Direction d) {
        direction.put(s, d);
    }

    public void addOrChangeSignalDirection(XbmSignal s, SignalState fromState, SignalState toState) {

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
        else {
            direction.put(s, Direction.CLEAR);
        }
    }

    public void setFrom(XbmState from) {
        this.from = from;
    }

    public void setTo(XbmState to) {
        this.to = to;
    }

    public void removeSignal(XbmSignal s) {
        if (direction.containsKey(s)) direction.remove(s);
    }

    public boolean containsDirectedDontCare() {
        for (Map.Entry<XbmSignal, Direction> entry: direction.entrySet()) {
            if (entry.getValue() == Direction.UNSTABLE) {
                return true;
            }
        }
        return false;
    }

    public String getAsString() {

        final Map<XbmSignal, Direction> targetSigs = new LinkedHashMap<>(direction);
        String inputs = "", outputs = "";

        for (Map.Entry<XbmSignal, Burst.Direction> entry: targetSigs.entrySet()) {
            XbmSignal xbmSignal = entry.getKey();
            Burst.Direction direction = entry.getValue();

            if (direction != Direction.CLEAR) {

                String signalName = xbmSignal.getName();
                switch (xbmSignal.getType()) {
                    case INPUT:
                        inputs = appendTargetToList(inputs, signalName + direction);
                        break;
                    case OUTPUT:
                        outputs = appendTargetToList(outputs, signalName + direction);
                        break;
                    case DUMMY: case CONDITIONAL:
                        break;
                    default:
                        throw new RuntimeException("An unknown xbmSignal type was detected for xbmSignal " + signalName);
                }
            }
        }
        if (!inputs.isEmpty() || !outputs.isEmpty()) return inputs + " / " + outputs;
        else return "" + VisualEvent.EPSILON_SYMBOL;
    }

    private static String appendTargetToList(final String list, final String target) {
        String newList = list;
        if (!list.isEmpty()) newList += ", ";
        newList += target;
        return newList;
    }
}
