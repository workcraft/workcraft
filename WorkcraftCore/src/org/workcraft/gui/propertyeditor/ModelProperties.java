package org.workcraft.gui.propertyeditor;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.util.Pair;

public class ModelProperties implements Properties {

    private final LinkedList<PropertyDescriptor> propertyDescriptors = new LinkedList<PropertyDescriptor>();

    public ModelProperties() {
    }

    // Combine descriptors, so several object refer to one property descriptor
    public ModelProperties(Collection<PropertyDescriptor> descriptors) {
        LinkedHashMap<Pair<String, Class<?>>, Set<PropertyDescriptor>> categories =
                new LinkedHashMap<Pair<String, Class<?>>, Set<PropertyDescriptor>>();

        for (PropertyDescriptor descriptor: descriptors) {
            if (descriptor.isCombinable()) {
                Pair<String, Class<?>> key = new Pair<String, Class<?>>(descriptor.getName(), descriptor.getType());
                Set<PropertyDescriptor> value = categories.get(key);
                if (value == null) {
                    value = new HashSet<PropertyDescriptor>();
                    categories.put(key, value);
                }
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

    public void addSorted(final Collection<PropertyDescriptor> descriptors) {
        if (descriptors != null) {
            LinkedList<PropertyDescriptor> sortedDescriptors = new LinkedList<PropertyDescriptor>(descriptors);
            Collections.sort(sortedDescriptors, new Comparator<PropertyDescriptor>() {
                @Override
                public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
                    return (o1.getName().compareTo(o2.getName()));
                }
            });
            propertyDescriptors.addAll(sortedDescriptors);
        }
    }

    public void remove(final PropertyDescriptor descriptor) {
        if (descriptor != null) {
            propertyDescriptors.remove(descriptor);
        }
    }

    public void removeByName(final String propertyName) {
        if (propertyName != null) {
            for (PropertyDescriptor descriptor: new LinkedList<PropertyDescriptor>(propertyDescriptors)) {
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
            for (PropertyDescriptor descriptor: new LinkedList<PropertyDescriptor>(propertyDescriptors)) {
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
