package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class PropertyDerivative implements PropertyDescriptor {
    final PropertyDescriptor descriptor;

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
    public boolean isWritable() {
        return descriptor.isWritable();
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
    public Object getValue() throws InvocationTargetException {
        return descriptor.getValue();
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        descriptor.setValue(value);
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return descriptor.getChoice();
    }

}
