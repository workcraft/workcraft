package org.workcraft.plugins.dtd.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.dom.Model;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.Signal.Type;

public class SignalTypePropertyDescriptor implements PropertyDescriptor {
    private final Model model;
    private final Signal signal;

    public SignalTypePropertyDescriptor(Model model, Signal signal) {
        this.model = model;
        this.signal = signal;
    }

    @Override
    public String getName() {
        return model.getName(signal) + " type";
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
        return false;
    }

    @Override
    public boolean isTemplatable() {
        return false;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return signal.getType();
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        signal.setType((Type) value);
    }

    @Override
    public Map<Type, String> getChoice() {
        Map<Type, String> result = new LinkedHashMap<>();
        for (Type item : Type.values()) {
            result.put(item, item.toString());
        }
        return result;
    }

}
