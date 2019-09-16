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
        TOGGLE("~"),
        UNSTABLE("*"),
        STABLE("!");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public Direction toggle() {
            switch (this) {
            case MINUS:
                return Direction.PLUS;
            case PLUS:
                return Direction.MINUS;
            default:
                return this;
            }
        }

        public static Direction convertFromString(String value) {
            if (value.equals(PLUS.toString())) {
                return PLUS;
            } else if (value.equals(MINUS.toString())) {
                return MINUS;
            } else if (value.equals(STABLE.toString())) {
                return STABLE;
            } else if (value.equals(UNSTABLE.toString())) {
                return UNSTABLE;
            } else if (value.equals(TOGGLE.toString())) {
                return TOGGLE;
            } else {
                throw new ArgumentException("An unknown direction was set for the signal.");
            }
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
            if (from.getEncoding().get(s) == SignalState.HIGH && to.getEncoding().get(s) == SignalState.LOW) {
                direction.put(s, Direction.MINUS);
            } else if (from.getEncoding().get(s) == SignalState.LOW && to.getEncoding().get(s) == SignalState.HIGH) {
                direction.put(s, Direction.PLUS);
            } else if (from.getEncoding().get(s) == SignalState.DDC) {
                direction.put(s, Direction.UNSTABLE);
            }
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

        if (fromState == toState) {
            direction.remove(s);
        } else {
            Direction dir;
            boolean isRising = (fromState == SignalState.LOW && toState == SignalState.HIGH) || (fromState == SignalState.DDC && toState == SignalState.HIGH);
            boolean isFalling = (fromState == SignalState.HIGH && toState == SignalState.LOW) || (fromState == SignalState.DDC && toState == SignalState.LOW);
            boolean isUnstable = fromState != SignalState.DDC && toState == SignalState.DDC;
            if (isRising && !isFalling && !isUnstable) {
                dir = Direction.PLUS;
            } else if (!isRising && isFalling && !isUnstable) {
                dir = Direction.MINUS;
            } else if (!isRising && !isFalling && isUnstable) {
                dir = Direction.UNSTABLE;
            } else { //No changes to be made
                dir = Direction.STABLE;
            }
            direction.put(s, dir);
        }
    }

    public void setFrom(XbmState from) {
        this.from = from;
    }

    public void setTo(XbmState to) {
        this.to = to;
    }

    public void removeSignal(XbmSignal s) {
        if (direction.containsKey(s)) {
            direction.remove(s);
        }
    }

    public String getAsString() {
        String inputs = getInputBurstAsString();
        String outputs = getOutputBurstAsString();
        if (!inputs.isEmpty() || !outputs.isEmpty()) {
            return inputs + " / " + outputs;
        } else {
            return "" + VisualEvent.EPSILON_SYMBOL;
        }
    }

    public String getInputBurstAsString() {
        return getBurst(XbmSignal.Type.INPUT);
    }

    public String getOutputBurstAsString() {
        return getBurst(XbmSignal.Type.OUTPUT);
    }

    private String getBurst(XbmSignal.Type type) {
        String burst = "";
        for (XbmSignal s: getSignals(type)) {
            if (direction.containsKey(s)) {
                if (!burst.isEmpty()) {
                    burst += ", ";
                }
                if (direction.get(s) != Direction.STABLE) {
                    burst += s.getName() + direction.get(s);
                }
            }
        }
        return burst;
    }
}
