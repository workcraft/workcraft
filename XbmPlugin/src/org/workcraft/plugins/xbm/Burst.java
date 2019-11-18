package org.workcraft.plugins.xbm;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fsm.VisualEvent;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

//FIXME PropertyChangedEvents are now updating slowly
public class Burst extends Symbol {

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

    public Map<XbmSignal, Direction> getDirections(XbmSignal.Type type) {
        Set<XbmSignal> signals = getSignals(type);
        Map<XbmSignal, Direction> result = new LinkedHashMap<>(direction);
        for (XbmSignal signal: signals) {
            result.remove(signal);
        }
        return result;
    }

    public Map<XbmSignal, Direction> getDirection() {
        return direction;
    }

    public Set<XbmSignal> getSignals() {
        return direction.keySet();
    }

    public Set<XbmSignal> getSignals(XbmSignal.Type type) {
        Set<XbmSignal> result = new HashSet<>(getSignals());
        result.removeIf(signal -> (signal != null) && signal.getType() != type);
        return result;
    }

    public XbmState getFrom() {
        return from;
    }

    public XbmState getTo() {
        return to;
    }

    public void addOrChangeSignalDirection(XbmSignal s, Direction d) {
        if (d != null) {
            direction.put(s, d);
        }
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
            sendNotification(new PropertyChangedEvent(this, Burst.PROPERTY_DIRECTION));
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
            if (direction.containsKey(s) && direction.get(s) != Direction.STABLE) {
                if (!burst.isEmpty()) {
                    burst += ", ";
                }
                burst += s.getName() + direction.get(s);
            }
        }
        return burst;
    }
}
