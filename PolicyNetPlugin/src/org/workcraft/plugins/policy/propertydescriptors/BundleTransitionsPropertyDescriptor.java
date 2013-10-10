
package org.workcraft.plugins.policy.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.PolicyNet;

public final class BundleTransitionsPropertyDescriptor implements PropertyDescriptor {
	private final PolicyNet model;
	private final Bundle bundle;

	public BundleTransitionsPropertyDescriptor(PolicyNet model, Bundle bundle) {
		this.model = model;
		this.bundle = bundle;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return model.getName(bundle) + " transitions";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return model.getTransitionsOfBundleAsString(bundle);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.setTransitionsOfBundleAsString(bundle, (String)value);
	}

	@Override
	public boolean isCombinable() {
		return false;
	}
}