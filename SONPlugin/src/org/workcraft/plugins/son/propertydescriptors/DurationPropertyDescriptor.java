package org.workcraft.plugins.son.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.util.Interval;

public class DurationPropertyDescriptor implements PropertyDescriptor{

    private final Time t;
    public static final String PROPERTY_DURATION = "Duration";

    public DurationPropertyDescriptor(Time t) {
        this.t = t;
    }

    @Override
    public String getName() {
        return PROPERTY_DURATION;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public boolean isWritable() {
        return false;
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
        Interval value = t.getDuration();
        return value.toString();
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        String input = (String) value;
        Interval result = new Interval(Interval.getMin(input), Interval.getMax(input));
        t.setDuration(result);
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return null;
    }

}
