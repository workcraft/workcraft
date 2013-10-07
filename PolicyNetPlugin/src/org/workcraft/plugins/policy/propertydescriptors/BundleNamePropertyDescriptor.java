
package org.workcraft.plugins.policy.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.PolicyNet;

public final class BundleNamePropertyDescriptor implements PropertyDescriptor {
	private final PolicyNet model;
	private final Bundle bundle;

	public BundleNamePropertyDescriptor(PolicyNet model, Bundle bundle) {
		this.model = model;
		this.bundle = bundle;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return model.getName(bundle) + " name";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return model.getName(bundle);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.setName(bundle, (String)value);
	}

	@Override
	public boolean isCombinable() {
		return false;
	}
}