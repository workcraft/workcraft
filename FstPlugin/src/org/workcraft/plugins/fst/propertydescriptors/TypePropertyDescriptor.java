package org.workcraft.plugins.fst.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.Signal.Type;

public class TypePropertyDescriptor implements PropertyDescriptor  {

	private final Signal signal;

	public TypePropertyDescriptor(Signal signal) {
		this.signal = signal;
	}

	@Override
	public String getName() {
		return Signal.PROPERTY_TYPE;
	}

	@Override
	public Class<?> getType() {
		return int.class;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public boolean isCombinable() {
		return true;
	}

	@Override
	public boolean isTemplatable() {
		return true;
	}

	@Override
	public Type getValue() throws InvocationTargetException {
		return signal.getType();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		signal.setType((Type)value);
	}

	@Override
	public Map<Type, String> getChoice() {
		Map<Type, String> result = new LinkedHashMap<Type, String>();
		for (Type item : Type.values()) {
			result.put(item, item.toString());
		}
		return result;
	}

}
