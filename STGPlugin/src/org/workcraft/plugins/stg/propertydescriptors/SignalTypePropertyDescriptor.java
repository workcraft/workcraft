package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class SignalTypePropertyDescriptor implements PropertyDescriptor  {
	private final STG stg;
	private final String signal;

	public SignalTypePropertyDescriptor(STG stg, String signal) {
		this.stg = stg;
		this.signal = signal;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return stg.getSignalType(signal);
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		stg.setSignalType(signal, (Type)value);
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
		return signal + " type";
	}

	@Override
	public Class<?> getType() {
		return int.class;
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

}
