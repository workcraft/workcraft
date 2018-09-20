package org.workcraft.plugins.stg.properties;

import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.SignalTransition;

public class SignalPropertyDescriptor implements PropertyDescriptor {
    private final Stg stg;
    private final SignalTransition transition;

    public SignalPropertyDescriptor(Stg stg, SignalTransition transition) {
        this.stg = stg;
        this.transition = transition;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return "Signal name";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return transition.getSignalName();
    }

    @Override
    public void setValue(Object value) {
        stg.setName(transition, (String) value);
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
        return false;
    }

}
