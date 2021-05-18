package org.workcraft.gui.properties;

import org.workcraft.types.Pair;

import java.util.*;

public class ModelProperties implements Properties {

    private final LinkedList<PropertyDescriptor> propertyDescriptors = new LinkedList<>();

    public ModelProperties() {
    }

    // Combine descriptors, so several object refer to one property descriptor
    public ModelProperties(Collection<PropertyDescriptor> descriptors) {
        LinkedHashMap<Pair<String, Class<?>>, Set<PropertyDescriptor>> categories = new LinkedHashMap<>();

        for (PropertyDescriptor descriptor: descriptors) {
            if (descriptor.isCombinable()) {
                Pair<String, Class<?>> key = new Pair<>(descriptor.getName(), descriptor.getType());
                Set<PropertyDescriptor> value = categories.computeIfAbsent(key, k -> new HashSet<>());
                value.add(descriptor);
            }
        }

        for (Pair<String, Class<?>> key: categories.keySet()) {
            final String name = key.getFirst();
            final Class<?> type = key.getSecond();
            final Set<PropertyDescriptor> values = categories.get(key);
            PropertyDescriptor comboDescriptor = new PropertyCombiner(name, type, values);
            propertyDescriptors.add(comboDescriptor);
        }
    }

    public void add(final PropertyDescriptor descriptor) {
        if (descriptor != null) {
            propertyDescriptors.add(descriptor);
        }
    }

    public void addAll(final Collection<PropertyDescriptor> descriptors) {
        if (descriptors != null) {
            propertyDescriptors.addAll(descriptors);
        }
    }

    public void remove(final PropertyDescriptor descriptor) {
        if (descriptor != null) {
            propertyDescriptors.remove(descriptor);
        }
    }

    public void removeByName(final String propertyName) {
        if (propertyName != null) {
            for (PropertyDescriptor descriptor : new LinkedList<>(propertyDescriptors)) {
                if ((descriptor != null) && propertyName.equals(descriptor.getName())) {
                    remove(descriptor);
                }
            }
        }
    }

    public void rename(final PropertyDescriptor descriptor, final String newPropertyName) {
        if (descriptor != null) {
            PropertyDerivative newDescriptor = new PropertyDerivative(descriptor) {
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
            for (PropertyDescriptor descriptor : new LinkedList<>(propertyDescriptors)) {
                if ((descriptor != null) && propertyName.equals(descriptor.getName())) {
                    rename(descriptor, newPropertyName);
                }
            }
        }
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return Collections.unmodifiableList(propertyDescriptors);
    }

}
