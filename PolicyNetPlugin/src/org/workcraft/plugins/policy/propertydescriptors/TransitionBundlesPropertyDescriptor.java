
package org.workcraft.plugins.policy.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.PolicyNet;

public final class TransitionBundlesPropertyDescriptor implements PropertyDescriptor {
	private final PolicyNet model;
	private final BundledTransition transition;

	public TransitionBundlesPropertyDescriptor(PolicyNet model, BundledTransition transition) {
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
		return model.getTransitionBundlesAsString(transition);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.setTransitionBundlesAsString(transition, (String)value);
	}
}