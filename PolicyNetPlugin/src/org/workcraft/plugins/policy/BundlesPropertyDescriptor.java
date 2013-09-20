
package org.workcraft.plugins.policy;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;

final class BundlesPropertyDescriptor implements PropertyDescriptor {
	private final PolicyNet model;
	private final BundledTransition transition;

	public BundlesPropertyDescriptor(PolicyNet model, BundledTransition transition) {
		this.model = model;
		this.transition = transition;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return "Bundles";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return model.getBundlesAsString(transition);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.setBundlesAsString(transition, (String)value);
	}
}