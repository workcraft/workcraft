package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class TypePropertyDescriptor implements PropertyDescriptor  {
    public static final String PROPERTY_SIGNAL_TYPE = "Signal type";

    private final Stg stg;
    private final SignalTransition transition;

    public TypePropertyDescriptor(Stg stg, SignalTransition transition) {
        this.stg = stg;
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

    @Override
    public Object getValue() throws InvocationTargetException {
        return transition.getSignalType();
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        transition.setSignalType((Type) value);
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
