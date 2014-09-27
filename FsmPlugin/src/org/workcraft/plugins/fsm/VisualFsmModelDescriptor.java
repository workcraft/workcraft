package org.workcraft.plugins.fsm;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class VisualFsmModelDescriptor implements VisualModelDescriptor {

	@Override
	public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
		VisualFsm fsm = new VisualFsm((Fsm)mathModel);
		fsm.createInitialState();
		return fsm;
	}

	@Override
	public Iterable<GraphEditorTool> createTools() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
