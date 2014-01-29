package org.workcraft.plugins.son;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class SONVisualModelDescriptor implements VisualModelDescriptor{

	public VisualModel create (MathModel mathModel) throws VisualModelInstantiationException{
		return new VisualSON((SON)mathModel);
	}

	@SuppressWarnings("deprecation")
	public Iterable<GraphEditorTool> createTools(){
		throw new org.workcraft.exceptions.NotImplementedException();
	}

}
