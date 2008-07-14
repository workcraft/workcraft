package org.workcraft.gui.edit.graph;

import java.util.HashSet;

import org.workcraft.dom.Component;
import org.workcraft.dom.AbstractGraphModel;

public class EditableComponent {
	protected AbstractGraphModel ownerDocument = null;
	protected HashSet<Component> children = new HashSet<Component>();

	public void addChild (Component component) {
		children.add(component);
	}


	public HashSet<Component> getChildren() {
		return (HashSet<Component>)children.clone();
	}
}
