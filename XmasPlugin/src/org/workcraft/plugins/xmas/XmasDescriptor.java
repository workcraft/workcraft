package org.workcraft.plugins.xmas;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class XmasDescriptor implements ModelDescriptor {

	@Override
	public MathModel createMathModel() {
		return new Xmas();
	}

	@Override
	public String getDisplayName() {
		return "xMAS Circuit";
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualXmasDescriptor();
	}

}
