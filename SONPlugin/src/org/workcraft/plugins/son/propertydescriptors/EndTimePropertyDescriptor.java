package org.workcraft.plugins.son.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.son.Time;

public class EndTimePropertyDescriptor implements PropertyDescriptor{
	private final Time t;

	public EndTimePropertyDescriptor(Time t) {
		this.t = t;
	}

	@Override
	public String getName() {
		return "End time";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

	@Override
	public boolean isTemplatable() {
		return false;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return t.getEndTime();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {

		t.setEndTime((String)value);
	}

	@Override
	public Map<? extends Object, String> getChoice() {
		return null;
	}

}
