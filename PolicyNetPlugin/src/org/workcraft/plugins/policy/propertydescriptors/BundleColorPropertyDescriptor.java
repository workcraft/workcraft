
package org.workcraft.plugins.policy.propertydescriptors;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.PolicyNet;

public final class BundleColorPropertyDescriptor implements PropertyDescriptor {
	private final PolicyNet model;
	private final Bundle bundle;

	public BundleColorPropertyDescriptor(PolicyNet model, Bundle bundle) {
		this.model = model;
		this.bundle = bundle;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return model.getName(bundle) + " color";
	}

	@Override
	public Class<?> getType() {
		return Color.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return bundle.getColor();
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		bundle.setColor((Color)value);
	}
}