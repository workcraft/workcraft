package org.workcraft.gui.properties;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class ModelProperties implements Properties {

    private final LinkedList<PropertyDescriptor<?>> propertyDescriptors = new LinkedList<>();

    public void add(final PropertyDescriptor<?> descriptor) {
        if (descriptor != null) {
            propertyDescriptors.add(descriptor);
        }
    }

    public void addAll(final Collection<PropertyDescriptor<?>> descriptors) {
        if (descriptors != null) {
            propertyDescriptors.addAll(descriptors);
        }
    }

    public void remove(final PropertyDescriptor<?> descriptor) {
        if (descriptor != null) {
            propertyDescriptors.remove(descriptor);
        }
    }

    public void removeByName(final String propertyName) {
        if (propertyName != null) {
            for (PropertyDescriptor<?> descriptor : new LinkedList<>(propertyDescriptors)) {
                if ((descriptor != null) && propertyName.equals(descriptor.getName())) {
                    remove(descriptor);
                }
            }
        }
    }

    public <V> void rename(final PropertyDescriptor<V> descriptor, final String newPropertyName) {
        if (descriptor != null) {
            PropertyDerivative<V> newDescriptor = new PropertyDerivative<>(descriptor) {
                @Override
                public String getName() {
                    return newPropertyName;
                }
            };
            remove(descriptor);
            add(newDescriptor);
        }
    }

    public void renameByName(final String propertyName, final String newPropertyName) {
        if (propertyName != null) {
            for (PropertyDescriptor<?> descriptor : new LinkedList<>(propertyDescriptors)) {
                if ((descriptor != null) && propertyName.equals(descriptor.getName())) {
                    rename(descriptor, newPropertyName);
                }
            }
        }
    }

    @Override
    public Collection<PropertyDescriptor<?>> getDescriptors() {
        return Collections.unmodifiableList(propertyDescriptors);
    }

}
