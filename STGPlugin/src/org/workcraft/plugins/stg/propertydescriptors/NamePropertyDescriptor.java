package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;

public class NamePropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final Node node;

	public NamePropertyDescriptor(STG stg, Node node) {
		this.stg = stg;
		this.node = node;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return "Name";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return stg.getName(node);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		stg.setName(node, (String)value);
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

}
