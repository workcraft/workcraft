package org.workcraft.dom.visual;

import org.workcraft.dom.HierarchyNode;
import org.workcraft.gui.propertyeditor.PropertyEditable;

public class TransformEventPropagator {
	public static void fireTransformChanged(HierarchyNode node)
	{
		if(node instanceof PropertyEditable && node instanceof Movable)
			((PropertyEditable)node).firePropertyChanged("transform");
		for(HierarchyNode n : node.getChildren())
			fireTransformChanged(n);
	}
}
