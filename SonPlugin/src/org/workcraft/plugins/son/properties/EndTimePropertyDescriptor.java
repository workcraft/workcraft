package org.workcraft.plugins.son.properties;

import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.util.Interval;

public class EndTimePropertyDescriptor implements PropertyDescriptor {
    private final Time t;
    public static final String PROPERTY_END_TIME = "End time";

    public EndTimePropertyDescriptor(Time t) {
        this.t = t;
    }

    @Override
    public String getName() {
        return PROPERTY_END_TIME;
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
    public Object getValue() {
        Interval value = t.getEndTime();
        return value.toString();
    }

    @Override
    public void setValue(Object value) {
        String input = (String) value;
        Interval result = new Interval(Interval.getMin(input), Interval.getMax(input));
        t.setEndTime(result);
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return null;
    }

}
