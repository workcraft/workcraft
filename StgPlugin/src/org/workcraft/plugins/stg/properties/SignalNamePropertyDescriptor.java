package org.workcraft.plugins.stg.properties;

import org.workcraft.dom.Container;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;

import java.util.Map;

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
    public void setValue(Object value) {
        if (!signal.equals(value)) {
            for (SignalTransition transition : stg.getSignalTransitions(signal, container)) {
                stg.setName(transition, (String) value);
            }
        }
    }

}
