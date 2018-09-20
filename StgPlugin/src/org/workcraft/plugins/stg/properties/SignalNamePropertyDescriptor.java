package org.workcraft.plugins.stg.properties;

import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.SignalTransition;

public class SignalNamePropertyDescriptor implements PropertyDescriptor {
    private final Stg stg;
    private final String signal;
    private final Container container;

    public SignalNamePropertyDescriptor(Stg stg, String signal, Container container) {
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
    public Object getValue() {
        return signal;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public void setValue(Object value) {
        if (!signal.equals(value)) {
            for (SignalTransition transition : stg.getSignalTransitions(signal, container)) {
                stg.setName(transition, (String) value);
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
