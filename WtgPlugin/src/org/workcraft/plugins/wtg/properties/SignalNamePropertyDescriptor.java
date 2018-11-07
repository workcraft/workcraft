package org.workcraft.plugins.wtg.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.wtg.Guard;
import org.workcraft.plugins.wtg.Waveform;
import org.workcraft.plugins.wtg.Wtg;

import java.util.Map;

public class SignalNamePropertyDescriptor implements PropertyDescriptor {

    private final Wtg wtg;
    private final String signalName;

    public SignalNamePropertyDescriptor(Wtg wtg, String signalName) {
        this.wtg = wtg;
        this.signalName = signalName;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return signalName + " name";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return signalName;
    }

    @Override
    public void setValue(Object value) {
        if ((value instanceof String) && (signalName != null) && !signalName.equals(value)) {
            for (Signal signal : wtg.getSignals()) {
                if (!signalName.equals(wtg.getName(signal))) continue;
                wtg.setName(signal, (String) value);
                signal.sendNotification(new PropertyChangedEvent(signal, Signal.PROPERTY_NAME));
            }
            for (Waveform waveform : wtg.getWaveforms()) {
                Guard guard = waveform.getGuard();
                if (!guard.containsKey(signalName)) continue;
                Guard newGuard = new Guard();
                for (Map.Entry<String, Boolean> entry : guard.entrySet()) {
                    String key = signalName.equals(entry.getKey()) ? (String) value : entry.getKey();
                    newGuard.put(key, entry.getValue());
                }
                waveform.setGuard(newGuard);
            }
        }
    }

}
