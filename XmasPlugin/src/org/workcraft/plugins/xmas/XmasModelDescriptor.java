package org.workcraft.plugins.xmas;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class XmasModelDescriptor implements ModelDescriptor {

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
		return new VisualModelDescriptor()
		{
			@Override
			public VisualModel create(MathModel mathModel)
					throws VisualModelInstantiationException {
				return new VisualXmas((Xmas)mathModel);
			}

			@Override
			public Iterable<GraphEditorTool> createTools() {
				try {
					throw new VisualModelInstantiationException();
				} catch (VisualModelInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		};
	}

}
