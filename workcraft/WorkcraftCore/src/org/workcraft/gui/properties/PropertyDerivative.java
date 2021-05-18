package org.workcraft.gui.properties;

import java.util.Map;

public class PropertyDerivative implements PropertyDescriptor {

    private final PropertyDescriptor descriptor;

    public PropertyDerivative(PropertyDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public Class<?> getType() {
        return descriptor.getType();
    }

    @Override
    public Object getValue() {
        return descriptor.getValue();
    }

    @Override
    public void setValue(Object value) {
        descriptor.setValue(value);
    }

    @Override
    public Map<?, String> getChoice() {
        return descriptor.getChoice();
    }

    @Override
    public boolean isEditable() {
        return descriptor.isEditable();
    }

    @Override
    public boolean isVisible() {
        return descriptor.isVisible();
    }

    @Override
    public boolean isCombinable() {
        return descriptor.isCombinable();
    }

    @Override
    public boolean isTemplatable() {
        return descriptor.isTemplatable();
    }

    @Override
    public boolean isSpan() {
        return descriptor.isSpan();
    }

}
