package org.workcraft.gui.properties;

import java.util.Map;

public class PropertySeparator implements PropertyDescriptor<String> {

    private String value;

    public PropertySeparator(final String value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class getType() {
        return String.class;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Map getChoice() {
        return null;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return true;
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
    public boolean isSpan() {
        return true;
    }

}
