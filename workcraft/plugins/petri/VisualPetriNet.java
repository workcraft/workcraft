package org.workcraft.plugins.petri;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.edit.tools.GraphEditorTool;

public class VisualPetriNet extends VisualModel {
	@Override
	public ArrayList<GraphEditorTool> getAdditionalTools() {
		ArrayList<GraphEditorTool> list = new ArrayList<GraphEditorTool>(super.getAdditionalTools());
		list.add(new SimulationTool());
		return list;
	}

	public VisualPetriNet(PetriNet model)
	throws VisualModelInstantiationException {
		super(model);
		try {
			createDefaultFlatStructure();
		} catch (VisualComponentCreationException e) {
			throw new VisualModelInstantiationException(e);
		} catch (VisualConnectionCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
	}

	public VisualPetriNet(PetriNet model, Element visualElement) throws VisualModelInstantiationException {
		super(model, visualElement);

	}

}