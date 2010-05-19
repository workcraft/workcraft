package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.STG;

public class DummyNamePropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final DummyTransition node;

	public DummyNamePropertyDescriptor(STG stg, DummyTransition node) {
		this.stg = stg;
		this.node = node;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return "Dummy name";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return node.getName();
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		stg.setName(node, (String)value);
	}
}