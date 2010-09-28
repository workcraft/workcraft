package org.workcraft.dom;

import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public interface VisualModelDescriptor {
	public VisualModel create (MathModel mathModel) throws VisualModelInstantiationException;
	public Iterable<GraphEditorTool> createTools();
}
