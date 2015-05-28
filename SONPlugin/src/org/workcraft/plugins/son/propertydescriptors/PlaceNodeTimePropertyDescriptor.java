package org.workcraft.plugins.son.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.son.elements.PlaceNode;

public class PlaceNodeTimePropertyDescriptor implements PropertyDescriptor{
	private final PlaceNode c;

	public PlaceNodeTimePropertyDescriptor(PlaceNode c) {
		this.c = c;
	}

	@Override
	public String getName() {
		return "Duration";
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
		return c.getDuration();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {

		c.setDuration((String)value);
	}

	@Override
	public Map<? extends Object, String> getChoice() {
		return null;
	}

}
