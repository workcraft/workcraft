package org.workcraft.plugins.dfs;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class VisualDfsModelDescriptor implements VisualModelDescriptor {

	@Override
	public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
		return new VisualDfs((Dfs)mathModel);
	}

	@Override
	public Iterable<GraphEditorTool> createTools() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
