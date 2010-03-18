package org.workcraft.plugins.stg;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

public class InstancePropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final SignalTransition st;

	public InstancePropertyDescriptor(STG stg, SignalTransition st) {
		this.stg = stg;
		this.st = st;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return stg.getInstanceNumber(st);
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		throw new NotSupportedException();
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return "Instance";
	}

	@Override
	public Class<?> getType() {
		return int.class;
	}
}
