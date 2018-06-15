package org.workcraft.plugins.fst.propertydescriptors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.fst.Signal;

public class TypePropertyDescriptor implements PropertyDescriptor {

    private final Signal signal;

    public TypePropertyDescriptor(Signal signal) {
        this.signal = signal;
    }

    @Override
    public String getName() {
        return Signal.PROPERTY_TYPE;
    }

    @Override
    public Class<?> getType() {
        return int.class;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return true;
    }

    @Override
    public boolean isTemplatable() {
        return true;
    }

    @Override
    public Signal.Type getValue() {
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
