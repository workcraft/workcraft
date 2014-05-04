package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class TypePropertyDescriptor implements PropertyDescriptor  {
	private final STG stg;
	private final SignalTransition transition;

	public TypePropertyDescriptor(STG stg, SignalTransition transition) {
		this.stg = stg;
		this.transition = transition;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return transition.getSignalType();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		transition.setSignalType((Type)value);
	}

	@Override
	public Map<Object, String> getChoice() {
		LinkedHashMap<Object, String> result = new LinkedHashMap<Object, String>();
		for (Type type: Type.values()) {
			result.put(type, type.toString());
		}
		return result;
	}

	@Override
	public String getName() {
		return "Signal type";
	}

	@Override
	public Class<?> getType() {
		return int.class;
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

}
