package org.workcraft.gui.edit.graph;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.Component;

public class EditableGraphModel {
	protected ComponentGroup root = new ComponentGroup();

	public List<Component> getTopLevelComponents() {
		LinkedList<Component> result = new LinkedList<Component>();
//		for (Component c : root.getChildren()) {
//			result.add(c);
//		}
		return result;
	}
	public ComponentGroup getRootGroup() {
		return root;
	}

}
