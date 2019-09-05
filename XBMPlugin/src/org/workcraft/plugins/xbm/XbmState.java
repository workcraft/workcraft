package org.workcraft.plugins.xbm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.fsm.State;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@DisplayName("State")
@IdentifierPrefix("s")
@VisualClass(VisualXbmState.class)
public class XbmState extends State {

    public static final String PROPERTY_ENCODING = "Encoding";
    public static final SignalState DEFAULT_SIGNAL_STATE = SignalState.LOW;

    private final Map<Signal, SignalState> encoding = new LinkedHashMap<>();

    public Map<Signal, SignalState> getEncoding() {
        return encoding;
    }

    public Set<Signal> getSignals() {
        return encoding.keySet();
    }

    public void addOrChangeSignalValue(Signal signal, SignalState state) {
        if (signal.getType() == Signal.Type.INPUT || signal.getType() == Signal.Type.OUTPUT) {
            encoding.put(signal, state);
        }
        PropertyChangedEvent pce = new PropertyChangedEvent(this, PROPERTY_ENCODING);
        sendNotification(pce);
    }

    public void removeSignal(Signal signal) {
        if (encoding.containsKey(signal)) {
            encoding.remove(signal);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ENCODING));
        }
    }

    public String getStateEncoding() {
        String input = "";
        String output = "";

        for (Signal s: getSignals()) {
            switch (s.getType()) {
                case INPUT:
                    if (encoding.get(s) == SignalState.DDC) input += "X";
                    else input += encoding.get(s);
                    break;

                case OUTPUT:
                    if (encoding.get(s) == SignalState.DDC) output += "X";
                    else output += encoding.get(s);
                    break;
            }
        }

        return input + output;
    }

    @Override
    public String toString() {
        String encoding = getStateEncoding();
        if (!encoding.isEmpty()) return encoding;
        else return super.toString();
    }
}
