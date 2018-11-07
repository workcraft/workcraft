package org.workcraft.plugins.stg.properties;

import org.workcraft.dom.Node;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;

import java.util.LinkedHashMap;
import java.util.Map;

public class DirectionPropertyDescriptor implements PropertyDescriptor {
    private final Stg stg;
    private final Node st;

    public DirectionPropertyDescriptor(Stg stg, Node st) {
        this.stg = stg;
        this.st = st;
    }

    @Override
    public String getName() {
        return SignalTransition.PROPERTY_DIRECTION;
    }

    @Override
    public Class<?> getType() {
        return int.class;
    }

    @Override
    public Object getValue() {
        return stg.getDirection(st);
    }

    @Override
    public void setValue(Object value) {
        stg.setDirection(st, (SignalTransition.Direction) value);
    }

    @Override
    public Map<SignalTransition.Direction, String> getChoice() {
        Map<SignalTransition.Direction, String> result = new LinkedHashMap<>();
        for (SignalTransition.Direction item: SignalTransition.Direction.values()) {
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
