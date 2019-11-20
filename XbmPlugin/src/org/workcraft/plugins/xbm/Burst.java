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
            SignalState fromState = from.getEncoding().get(s);
            SignalState toState = to.getEncoding().get(s);
            direction.put(s, fromState.determineDirectionBetween(toState));
        }
    }

    public Map<XbmSignal, Direction> getDirections(XbmSignal.Type type) {
        Map<XbmSignal, Direction> result = new LinkedHashMap<>(direction);
        result.entrySet().removeIf(xbmSignalDirectionEntry -> xbmSignalDirectionEntry.getKey().getType() != type);
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
        if (fromState == toState && fromState != SignalState.DDC) {
            direction.remove(s);
        } else {
            direction.put(s, fromState.determineDirectionBetween(toState));
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
