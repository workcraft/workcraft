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
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.util.Hierarchy;

@DisplayName ("Petri Net")
@CustomTools ( PetriNetToolProvider.class )
public class VisualPetriNet extends AbstractVisualModel {

	public VisualPetriNet(PetriNet model) {
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

	public VisualPlace createPlace(String mathName, Container container) {
		if (container == null) {
			container = getRoot();
		}
		Container mathContainer = NamespaceHelper.getMathContainer(this, container);
		Place place = getPetriNet().createPlace(mathName, mathContainer);
		VisualPlace visualPlace = new VisualPlace(place);
		container.add(visualPlace);
		return visualPlace;
	}

	public VisualTransition createTransition(String mathName, Container container) {
		if (container == null) {
			container = getRoot();
		}
		Container mathContainer = NamespaceHelper.getMathContainer(this, container);
		Transition transition = getPetriNet().createTransition(mathName, mathContainer);
		VisualTransition visualTransition = new VisualTransition(transition);
		add(visualTransition);
		return visualTransition;
	}

	public void validateConnection(Node first, Node second) throws InvalidConnectionException {
		if (getConnection(first, second) != null) {
			throw new InvalidConnectionException ("This arc already exists.");
		}
		if (first instanceof VisualPlace && second instanceof VisualPlace) {
			throw new InvalidConnectionException ("Arcs between places are not allowed.");
		}
		if (first instanceof VisualTransition && second instanceof VisualTransition) {
			throw new InvalidConnectionException ("Arcs between transitions are not allowed.");
		}
	}

	@Override
	public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualComponent c1 = (VisualComponent) first;
		VisualComponent c2 = (VisualComponent) second;

		if (mConnection == null) {
			PetriNet petriNet = (PetriNet)getMathModel();
			mConnection = petriNet.connect(c1.getReferencedComponent(), c2.getReferencedComponent());
		}
		VisualConnection ret = new VisualConnection(mConnection, c1, c2);

		Hierarchy.getNearestContainer(c1, c2).add(ret);
		return ret;
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
