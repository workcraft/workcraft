package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

public final class PropertyCombiner implements PropertyDescriptor {
	private final String name;
	private final Class<?> type;
	private final Set<PropertyDescriptor> values;

	public PropertyCombiner(String name,	Class<?> type, Set<PropertyDescriptor> values) {
		this.name = name;
		this.type = type;
		this.values = values;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		Object result = null;
		for (PropertyDescriptor descriptor: values) {
			if (result == null) {
				result = descriptor.getValue();
			} else if (!result.equals(descriptor.getValue())) {
				return null;
			}
		}
		return result;
	}

	@Override
	public void setValue(Object value)  throws InvocationTargetException {
		for (PropertyDescriptor descriptor: values) {
			descriptor.setValue(value);
		}
	}

	@Override
	public Map<? extends Object, String> getChoice() {
		Map<? extends Object, String> result = null;
		for (PropertyDescriptor descriptor: values) {
			result = descriptor.getChoice();
		}
		return result;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public boolean isWritable() {
		boolean result = true;
		for (PropertyDescriptor descriptor: values) {
			result = result && descriptor.isWritable();
		}
		return result;
	}

	@Override
	public boolean isCombinable() {
		boolean result = true;
		for (PropertyDescriptor descriptor: values) {
			result = result && descriptor.isCombinable();
		}
		return result;
	}

	@Override
	public boolean isTemplatable() {
		boolean result = true;
		for (PropertyDescriptor descriptor: values) {
			result = result && descriptor.isTemplatable();
		}
		return result;
	}

}
