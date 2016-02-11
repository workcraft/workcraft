package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;

public class SignalNamePropertyDescriptor implements PropertyDescriptor {
    private final STG stg;
    private final String signal;
    private final Container container;

    public SignalNamePropertyDescriptor(STG stg, String signal, Container container) {
        this.stg = stg;
        this.signal = signal;
        this.container = container;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return signal + " name";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return signal;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        if (!signal.equals(value)) {
            for (SignalTransition transition : stg.getSignalTransitions(signal, container)) {
                stg.setName(transition, (String)value);
            }
        }
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
