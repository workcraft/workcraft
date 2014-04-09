package org.workcraft.plugins.circuit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;

public class EnvironmentFilePropertyDescriptor implements PropertyDescriptor {
	VisualCircuit model;

	public EnvironmentFilePropertyDescriptor(VisualCircuit model) {
		this.model = model;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return "Environment URI";
	}

	@Override
	public Class<?> getType() {
		return URI.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return model.getEnvironmentFile();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.setEnvironmentFile((File)value);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

}
