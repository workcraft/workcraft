
package org.workcraft.plugins.petri;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

final class NamePropertyDescriptor implements PropertyDescriptor {
	private final PetriNet model;
	private final Node node;

	public NamePropertyDescriptor(PetriNet model, Node node) {
		this.model = model;
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
		return model.getName(node);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		model.setName(node, (String)value);
	}
}