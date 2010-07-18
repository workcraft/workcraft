
package org.workcraft.plugins.petri;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

final class NamePropertyDescriptor implements PropertyDescriptor {

	private final PetriNet petriNet;

	public NamePropertyDescriptor(PetriNet petriNet, Node node)
	{
		this.petriNet = petriNet;
		this.node = node;
	}

	private final Node node;

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
		return this.petriNet.getName(node);
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		this.petriNet.setName(node, (String)value);
	}
}