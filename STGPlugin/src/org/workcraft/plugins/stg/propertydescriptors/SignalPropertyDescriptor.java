package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;

public class SignalPropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final SignalTransition transition;

	public SignalPropertyDescriptor(STG stg, SignalTransition transition) {
		this.stg = stg;
		this.transition = transition;
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
		return transition.getSignalName();
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		transition.setSignalName((String)value);
		stg.setName(transition, (String)value);
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

}
