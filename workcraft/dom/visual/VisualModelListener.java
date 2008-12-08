package org.workcraft.dom.visual;

import org.workcraft.dom.MathModelListener;

public interface VisualModelListener extends MathModelListener{
	public void visualNodePropertyChanged(VisualNode n);
	public void layoutChanged();
}