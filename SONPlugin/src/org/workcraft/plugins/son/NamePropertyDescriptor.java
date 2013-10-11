
package org.workcraft.plugins.son;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

final class NamePropertyDescriptor implements PropertyDescriptor {

	private final SON net;

	public NamePropertyDescriptor(SON net, Node node)
	{
		this.net = net;
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
		return this.net.getName(node);
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		this.net.setName(node, (String)value);
	}

	@Override
	public boolean isCombinable() {
		return false;
	}
}