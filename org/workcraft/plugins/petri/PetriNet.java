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

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass ("org.workcraft.plugins.petri.VisualPetriNet")
@DisplayName("Petri Net")
public class PetriNet extends AbstractMathModel {
	private ReferenceManager<Node> referenceManager = new ReferenceManager<Node>(new Func<Node, String>() {
		@Override
		public String eval(Node arg) {
			if (arg instanceof Place) {
				final String label = ((Place)arg).getLabel();
				return "p:" + label;
			} else if (arg instanceof Transition) {
				final String label = ((Transition)arg).getLabel();
				return "t:" + label;
			}
			return "";
		}
	});

	public PetriNet() {
		this(null);
	}

	public PetriNet(Container root) {
		this(root, null);

	}

	public PetriNet(Container root, References refs) {
		super(root);
		new DefaultHangingConnectionRemover(this, "PN").attach(getRoot());
		ReferenceManager.attach(referenceManager, root, refs);
	}

	public void validate() throws ModelValidationException {
	}

	final public Place createPlace() {
		Place newPlace = new Place();
		getRoot().add(newPlace);
		return newPlace;
	}

	final public Transition createTransition() {
		Transition newTransition = new Transition();
		getRoot().add(newTransition);
		return newTransition;
	}

	final public Collection<Place> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
	}

	final public Collection<Transition> getTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	final public boolean isEnabled (Transition t) {
		for (Node n : getPreset(t))
			if (((Place)n).getTokens() <= 0)
				return false;
		return true;
	}

	final public void fire (Transition t) {
		if (isEnabled(t))
		{
			for (Node n : getPostset(t))
				((Place)n).setTokens(((Place)n).getTokens()+1);
			for (Node n : getPreset(t))
				((Place)n).setTokens(((Place)n).getTokens()-1);
		}
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		if (first instanceof Place && second instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (first instanceof Transition && second instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}

	@Override
	public Node getNodeByID(String ID) {
		return referenceManager.getObject(ID);
	}

	@Override
	public String getNodeID(Node node) {
		return referenceManager.getReference(node);
	}
}