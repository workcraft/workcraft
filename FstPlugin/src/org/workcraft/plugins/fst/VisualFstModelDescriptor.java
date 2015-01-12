package org.workcraft.plugins.fst;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class VisualFstModelDescriptor implements VisualModelDescriptor {

	@Override
	public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
		return new VisualFst((Fst)mathModel);
	}

	@Override
	public Iterable<GraphEditorTool> createTools() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
