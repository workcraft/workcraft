package org.workcraft.plugins.wtg.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.wtg.Wtg;

import java.util.LinkedHashMap;
import java.util.Map;

public class SignalTypePropertyDescriptor implements PropertyDescriptor {
    private final Wtg wtg;
    private final String signalName;

    public SignalTypePropertyDescriptor(Wtg wtg, String signalName) {
        this.wtg = wtg;
        this.signalName = signalName;
    }

    @Override
    public Object getValue() {
        if (signalName != null) {
            for (Signal signal : wtg.getSignals()) {
                if (!signalName.equals(wtg.getName(signal))) continue;
                return signal.getType();
            }
        }
        return null;
    }

    @Override
    public void setValue(Object value) {
        if ((value instanceof Signal.Type) && (signalName != null)) {
            Signal.Type type = (Signal.Type) value;
            for (Signal signal : wtg.getSignals()) {
                if (!signalName.equals(wtg.getName(signal))) continue;
                signal.setType(type);
            }
        }
    }

    @Override
    public Map<Object, String> getChoice() {
        LinkedHashMap<Object, String> result = new LinkedHashMap<>();
        for (Signal.Type type: Signal.Type.values()) {
            result.put(type, type.toString());
        }
        return result;
    }

    @Override
    public String getName() {
        return signalName + " type";
    }

    @Override
    public Class<?> getType() {
        return int.class;
    }

}
