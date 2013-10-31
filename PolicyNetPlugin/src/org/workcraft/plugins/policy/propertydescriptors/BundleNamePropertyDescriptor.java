
package org.workcraft.plugins.policy.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.policy.VisualBundle;
import org.workcraft.plugins.policy.VisualPolicyNet;

public final class BundleNamePropertyDescriptor implements PropertyDescriptor {
	private final VisualPolicyNet model;
	private final VisualBundle bundle;

	public BundleNamePropertyDescriptor(VisualPolicyNet model, VisualBundle bundle) {
		this.model = model;
		this.bundle = bundle;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return model.getPolicyNet().getName(bundle.getReferencedBundle()) + " name";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return model.getPolicyNet().getName(bundle.getReferencedBundle());
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.getPolicyNet().setName(bundle.getReferencedBundle(), (String)value);
	}

	@Override
	public boolean isCombinable() {
		return false;
	}
}