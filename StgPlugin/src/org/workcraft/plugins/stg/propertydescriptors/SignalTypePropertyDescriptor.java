package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class SignalTypePropertyDescriptor implements PropertyDescriptor  {
    private final Stg stg;
    private final String signal;
    private final Container container;

    public SignalTypePropertyDescriptor(Stg stg, String signal, Container container) {
        this.stg = stg;
        this.signal = signal;
        this.container = container;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return stg.getSignalType(signal, container);
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        stg.setSignalType(signal, (Type) value, container);
    }

    @Override
    public Map<Object, String> getChoice() {
        LinkedHashMap<Object, String> result = new LinkedHashMap<>();
        for (Type type: Type.values()) {
            result.put(type, type.toString());
        }
        return result;
    }

    @Override
    public String getName() {
        return signal + " type";
    }

    @Override
    public Class<?> getType() {
        return int.class;
    }

    @Override
    public boolean isCombinable() {
        return false;
    }

    @Override
    public boolean isTemplatable() {
        return false;
    }

}
