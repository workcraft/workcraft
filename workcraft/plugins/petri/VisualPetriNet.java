package org.workcraft.plugins.petri;

import org.workcraft.dom.DefaultMathNodeRemover;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.CustomToolButtons;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.framework.DefaultCreateButtons;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.NodeCreationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

@DisplayName ("Petri Net")
@DefaultCreateButtons ( { Place.class, Transition.class } )
@CustomToolButtons ( { SimulationTool.class } )
public class VisualPetriNet extends AbstractVisualModel {
	public VisualPetriNet(PetriNet model) throws VisualModelInstantiationException {
		this (model, null);
	}

	public VisualPetriNet (PetriNet model, VisualGroup root) throws VisualModelInstantiationException {
		super(model, root);

		if (root == null)
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new VisualModelInstantiationException(e);
			}

		new DefaultHangingConnectionRemover(this).attach(getRoot());
		new DefaultMathNodeRemover().attach(getRoot());
	}

	public void validate() throws ModelValidationException {
		getMathModel().validate();
	}

	public void validateConnection(Node first, Node second)
	throws InvalidConnectionException {
		if (first instanceof VisualPlace && second instanceof VisualPlace)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (first instanceof VisualTransition && second instanceof VisualTransition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}
}