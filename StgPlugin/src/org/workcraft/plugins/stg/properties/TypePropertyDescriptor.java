package org.workcraft.plugins.stg.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;

import java.util.LinkedHashMap;
import java.util.Map;

public class TypePropertyDescriptor implements PropertyDescriptor {
    public static final String PROPERTY_SIGNAL_TYPE = "Signal type";

    private final SignalTransition transition;

    public TypePropertyDescriptor(Stg stg, SignalTransition transition) {
        this.transition = transition;
    }

    @Override
    public String getName() {
        return PROPERTY_SIGNAL_TYPE;
    }

    @Override
    public Class<?> getType() {
        return int.class;
    }

    @Override
    public Object getValue() {
        return transition.getSignalType();
    }

    @Override
    public void setValue(Object value) {
        transition.setSignalType((Signal.Type) value);
    }

    @Override
    public Map<Signal.Type, String> getChoice() {
        Map<Signal.Type, String> result = new LinkedHashMap<>();
        for (Signal.Type item : Signal.Type.values()) {
            result.put(item, item.toString());
        }
        return result;
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
