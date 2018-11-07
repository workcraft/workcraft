package org.workcraft.plugins.son.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.util.Interval;

import java.util.Map;

public class DurationPropertyDescriptor implements PropertyDescriptor {

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
    public Object getValue() {
        Interval value = t.getDuration();
        return value.toString();
    }

    @Override
    public void setValue(Object value) {
        String input = (String) value;
        Interval result = new Interval(Interval.getMin(input), Interval.getMax(input));
        t.setDuration(result);
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

}
