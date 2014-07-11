package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Node;

public class DefaultNamePropertyDescriptor implements PropertyDescriptor {
	private final AbstractModel model;
	private final Node node;

	public DefaultNamePropertyDescriptor(AbstractModel model, Node node) {
		this.model = model;
		this.node = node;
	}


	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return model.getName(node);
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.setName(node, (String)value);

	}

	@Override
	public Map<? extends Object, String> getChoice() {
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
}
