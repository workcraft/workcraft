package org.workcraft.plugins.fst.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.Signal;

import java.util.LinkedHashMap;
import java.util.Map;

public class SignalTypePropertyDescriptor implements PropertyDescriptor {
    private final Fst fst;
    private final Signal signal;

    public SignalTypePropertyDescriptor(Fst fst, Signal signal) {
        this.fst = fst;
        this.signal = signal;
    }

    @Override
    public String getName() {
        return fst.getName(signal) + " type";
    }

    @Override
    public Class<?> getType() {
        return int.class;
    }

    @Override
    public Object getValue() {
        return signal.getType();
    }

    @Override
    public void setValue(Object value) {
        signal.setType((Signal.Type) value);
    }

    @Override
    public Map<Signal.Type, String> getChoice() {
        Map<Signal.Type, String> result = new LinkedHashMap<>();
        for (Signal.Type item : Signal.Type.values()) {
            result.put(item, item.toString());
        }
        return result;
    }

}
