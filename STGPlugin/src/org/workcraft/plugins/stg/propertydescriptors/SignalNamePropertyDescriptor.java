package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;

public class SignalNamePropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final SignalTransition node;

	public SignalNamePropertyDescriptor(STG stg, SignalTransition node) {
		this.stg = stg;
		this.node = node;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return "Signal name";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return node.getSignalName();
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		stg.setName(node, (String)value);
	}

	@Override
	public boolean isCombinable() {
		return true;
	}
}