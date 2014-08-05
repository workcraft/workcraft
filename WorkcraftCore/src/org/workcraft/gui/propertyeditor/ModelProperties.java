package org.workcraft.gui.propertyeditor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.util.Pair;

public class ModelProperties implements Properties {

	private final LinkedList<PropertyDescriptor> propertyDescriptors = new LinkedList<PropertyDescriptor>();

	public ModelProperties(PropertyDescriptor... descriptors) {
		for (PropertyDescriptor descriptor : descriptors) {
			propertyDescriptors.add(descriptor);
		}
	}

	public ModelProperties(Properties properties1, Properties properties2) {
		propertyDescriptors.addAll(properties1.getDescriptors());
		propertyDescriptors.addAll(properties2.getDescriptors());
	}

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

	public Collection<PropertyDescriptor> getDescriptors() {
		return Collections.unmodifiableList(propertyDescriptors);
	}

	public void add(final Properties properties) {
		if (properties != null) {
			for (PropertyDescriptor descriptor : properties.getDescriptors()) {
				propertyDescriptors.add(descriptor);
			}
		}
	}

	public void add(final PropertyDescriptor descriptor) {
		if (descriptor != null) {
			add(new ModelProperties(descriptor));
		}
	}

	public void remove(final PropertyDescriptor descriptor) {
		if (descriptor != null) {
			propertyDescriptors.remove(descriptor);
		}
	}

	public void filter(String... propertyNames) {
		HashSet<String> propertyNameSet = new HashSet<String>(Arrays.asList(propertyNames));
		for (PropertyDescriptor descriptor: new LinkedList<PropertyDescriptor>(propertyDescriptors)) {
			if ((descriptor != null) && propertyNameSet.contains(descriptor.getName())) {
				remove(descriptor);
			}
		}
	}

}
