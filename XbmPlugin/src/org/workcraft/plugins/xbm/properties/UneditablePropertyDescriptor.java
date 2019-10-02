package org.workcraft.plugins.xbm.properties;

import org.workcraft.gui.properties.PropertyDescriptor;

import java.util.Map;

public class UneditablePropertyDescriptor implements PropertyDescriptor {

    private final String propertyName;
    private final String value;

    public UneditablePropertyDescriptor(final String propertyName, final String value) {
        this.propertyName = propertyName;
        this.value = value;
    }

    @Override
    public String getName() {
        return propertyName;
    }

    @Override
    public Class getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
    }

    @Override
    public Map getChoice() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
