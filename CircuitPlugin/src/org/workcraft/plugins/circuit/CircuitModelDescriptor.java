package org.workcraft.plugins.circuit;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class CircuitModelDescriptor implements ModelDescriptor {

	@Override
	public MathModel createMathModel() {
		return new Circuit();
	}

	@Override
	public String getDisplayName() {
		return "Digital Circuit";
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualModelDescriptor()
		{
			@Override
			public VisualModel create(MathModel mathModel)
					throws VisualModelInstantiationException {
				return new VisualCircuit((Circuit)mathModel);
			}

			@Override
			public Iterable<GraphEditorTool> createTools() {
				throw new NotImplementedException();
			}
		};
	}

}
