package org.workcraft.plugins.dtd.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.dtd.Transition;
import org.workcraft.plugins.dtd.Transition.Direction;

public class DirectionPropertyDescriptor implements PropertyDescriptor {
    private final Transition transition;

    public DirectionPropertyDescriptor(Transition transition) {
        this.transition = transition;
    }

    @Override
    public String getName() {
        return Transition.PROPERTY_DIRECTION;
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
        return true;
    }

    @Override
    public Object getValue() {
        return transition.getDirection();
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        transition.setDirection((Direction) value);
    }

    @Override
    public Map<Direction, String> getChoice() {
        Map<Direction, String> result = new LinkedHashMap<Direction, String>();
        for (Direction item : Direction.values()) {
            result.put(item, item.toString());
        }
        return result;
    }

}
