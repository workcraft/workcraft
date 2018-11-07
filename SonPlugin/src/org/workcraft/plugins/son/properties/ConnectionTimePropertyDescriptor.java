package org.workcraft.plugins.son.properties;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.util.Interval;

import java.util.Map;

public class ConnectionTimePropertyDescriptor implements PropertyDescriptor {
    private final VisualSONConnection con;
    public static final String PROPERTY_CONNECTION_TIME = "Time interval";

    public ConnectionTimePropertyDescriptor(VisualSONConnection con) {
        this.con = con;
    }

    @Override
    public String getName() {
        return PROPERTY_CONNECTION_TIME;
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
        Interval value = con.getReferencedSONConnection().getTime();
        return value.toString();
    }

    @Override
    public void setValue(Object value) {
        String input = (String) value;
        Interval result = new Interval(Interval.getMin(input), Interval.getMax(input));
        con.getReferencedSONConnection().setTime(result);
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return null;
    }

}
