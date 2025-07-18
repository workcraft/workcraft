package org.workcraft.gui.properties;

import java.util.Map;

public class PropertyDerivative<V> implements PropertyDescriptor<V> {

    private final PropertyDescriptor<V> descriptor;

    public PropertyDerivative(PropertyDescriptor<V> descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return descriptor.getName();
    }

    @Override
    public Class<V> getType() {
        return descriptor.getType();
    }

    @Override
    public V getValue() {
        return descriptor.getValue();
    }

    @Override
    public void setValue(V value) {
        descriptor.setValue(value);
    }

    @Override
    public Map<V, String> getChoice() {
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
