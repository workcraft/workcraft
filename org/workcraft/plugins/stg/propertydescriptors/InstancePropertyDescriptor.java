package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;

public class InstancePropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final Node st;

	public InstancePropertyDescriptor(STG stg, Node st) {
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
		//stg.setInstanceNumber(st, Integer.parseInt(value.toString()));
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
