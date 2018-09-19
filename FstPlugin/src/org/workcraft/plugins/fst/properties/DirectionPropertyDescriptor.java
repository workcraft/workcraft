package org.workcraft.plugins.fst.properties;

import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fst.SignalEvent;
import org.workcraft.plugins.fst.SignalEvent.Direction;

public class DirectionPropertyDescriptor implements PropertyDescriptor {
    private final Event event;

    public DirectionPropertyDescriptor(Event event) {
        this.event = event;
    }

    @Override
    public String getName() {
        return SignalEvent.PROPERTY_DIRECTION;
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
        if (event instanceof SignalEvent) {
            SignalEvent signalEvent = (SignalEvent) event;
            return signalEvent.getDirection();
        }
        return null;
    }

    @Override
    public void setValue(Object value) {
        if (event instanceof SignalEvent) {
            SignalEvent signalEvent = (SignalEvent) event;
            signalEvent.setDirection((Direction) value);
        }
    }

    @Override
    public Map<Direction, String> getChoice() {
        Map<Direction, String> result = null;
        if (event instanceof SignalEvent) {
            result = new LinkedHashMap<>();
            for (Direction item : Direction.values()) {
                result.put(item, item.toString());
            }
        }
        return result;
    }

}
