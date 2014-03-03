/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.petri;

import java.util.Collection;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.Hierarchy;

@DisplayName ("Petri Net")
@CustomTools ( PetriNetToolProvider.class )
public class VisualPetriNet extends AbstractVisualModel {

	public VisualPetriNet(PetriNet model) throws VisualModelInstantiationException {
		this (model, null);
	}

	public VisualPetriNet (PetriNet model, VisualGroup root) {
		super(model, root);
		if (root == null) {
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public PetriNet getPetriNet() {
		return (PetriNet)getMathModel();
	}

	public VisualPlace createPlace(Place place) {
		VisualPlace visualPlace = new VisualPlace(place);
		add(visualPlace);
		return visualPlace;
	}

	public VisualPlace createPlace(String name) {
		return createPlace(getPetriNet().createPlace(name));
	}

	public VisualTransition createTransition(Transition transition) {
		VisualTransition visualTransition = new VisualTransition(transition);
		add(visualTransition);
		return visualTransition;
	}

	public VisualTransition createTransition(String name) {
		return createTransition(getPetriNet().createTransition(name));
	}

	public void validateConnection(Node first, Node second) throws InvalidConnectionException {
		if (first instanceof VisualPlace && second instanceof VisualPlace)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (first instanceof VisualTransition && second instanceof VisualTransition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}

	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualComponent c1 = (VisualComponent) first;
		VisualComponent c2 = (VisualComponent) second;

		PetriNet net = (PetriNet)getMathModel();
		MathConnection con = (MathConnection) net.connect(c1.getReferencedComponent(), c2.getReferencedComponent());

		VisualConnection ret = new VisualConnection(con, c1, c2);

		Hierarchy.getNearestContainer(c1, c2).add(ret);
	}

	public Collection<VisualPlace> getVisualPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualPlace.class);
	}

	public Collection<VisualTransition> getVisualTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualTransition.class);
	}

	public VisualTransition getVisualTransition(Transition transition) {
		for (VisualTransition vt: getVisualTransitions()) {
			if (vt.getReferencedTransition() == transition) {
				return vt;
			}
		}
		return null;
	}

}
