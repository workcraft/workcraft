package org.workcraft.plugins.petri;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.edit.tools.GraphEditorTool;

public class VisualPetriNet extends VisualModel {
	@Override
	public ArrayList<Class<? extends GraphEditorTool>> getAdditionalToolClasses() {
		ArrayList<Class<? extends GraphEditorTool>> list = new ArrayList<Class<? extends GraphEditorTool>>(super.getAdditionalToolClasses());
		list.add(SimulationTool.class);
		return list;
	}

	public VisualPetriNet(PetriNet model)
	throws VisualModelInstantiationException {
		super(model);
	}

	public VisualPetriNet(PetriNet model, Element visualElement) throws VisualModelInstantiationException {
		super(model, visualElement);

	}
}