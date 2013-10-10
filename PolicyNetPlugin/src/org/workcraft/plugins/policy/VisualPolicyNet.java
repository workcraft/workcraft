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

package org.workcraft.plugins.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.Hierarchy;

@DisplayName ("Policy Net")
@CustomTools ( PolicyNetToolProvider.class )
public class VisualPolicyNet extends VisualPetriNet {

	public VisualPolicyNet(PolicyNet model) throws VisualModelInstantiationException {
		this(model, null);
	}

	public VisualPolicyNet (PolicyNet model, VisualGroup root) {
		super(model, (root == null ? new VisualLocality((Locality)model.getRoot()) : root));
	}

	public PolicyNet getPolicyNet() {
		return (PolicyNet)getMathModel();
	}

	@Override
	public void groupSelection(){
		ArrayList<Node> selected = new ArrayList<Node>();
		ArrayList<Node> refSelected = new ArrayList<Node>();
		for(Node node : getOrderedCurrentLevelSelection()) {
			if(node instanceof VisualTransformableNode) {
				selected.add((VisualTransformableNode)node);
				if (node instanceof VisualComponent) {
					refSelected.add(((VisualComponent) node).getReferencedComponent());
				} else if (node instanceof VisualLocality) {
					refSelected.add(((VisualLocality) node).getLocality());
				}
			}
		}

		if (selected.size() > 0) {
			VisualLocality currentLevel = ((VisualLocality)getCurrentLevel());
			Locality refLocality = ((PolicyNet)getMathModel()).createLocality(refSelected, currentLevel.getLocality());

			VisualLocality locality = new VisualLocality(refLocality);
			currentLevel.add(locality);
			currentLevel.reparent(selected, locality);

			ArrayList<Node> connectionsToLocality = new ArrayList<Node>();
			for (VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class)) {
				if (Hierarchy.isDescendant(connection.getFirst(), locality) && Hierarchy.isDescendant(connection.getSecond(), locality)) {
					connectionsToLocality.add(connection);
				}
			}
			currentLevel.reparent(connectionsToLocality, locality);
			select(locality);
		}
	}

	@Override
	public void ungroupSelection() {
		int count = 0;
		for(Node node : getOrderedCurrentLevelSelection()){
			if(node instanceof VisualLocality) {
				count++;
			}
		}
		if (count == 1) {
			ArrayList<Node> toSelect = new ArrayList<Node>();
			Collection<Node> mathNodes = new ArrayList<Node>();
			for(Node node : getOrderedCurrentLevelSelection()) {
				if(node instanceof VisualLocality) {
					VisualLocality locality = (VisualLocality)node;
					for(Node subNode : locality.unGroup()){
						toSelect.add(subNode);
					}
					for(Node child : locality.getLocality().getChildren()){
						mathNodes.add(child);
					}
					locality.getLocality().reparent(mathNodes, ((VisualLocality)getCurrentLevel()).getLocality());
					getMathModel().remove(locality.getLocality());
					getCurrentLevel().remove(locality);

				} else {
					toSelect.add(node);
				}
			}
			select(toSelect);
		}
	}

	public Collection<VisualPlace> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualPlace.class);
	}

	public Collection<VisualBundledTransition> getBundledTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualBundledTransition.class);
	}

	public Collection<VisualLocality> getLocalities() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualLocality.class);
	}

	public Collection<Bundle> getBundlesOfTransition(VisualBundledTransition t) {
		Collection<Bundle> result = new HashSet<Bundle>();
		if (t != null) {
			result.addAll(getPolicyNet().getBundlesOfTransition(t.getReferencedTransition()));
		}
		return result;
	}

	public Collection<VisualBundledTransition> getTransitionsOfBundle(Bundle b) {
		Collection<VisualBundledTransition> result = new HashSet<VisualBundledTransition>();
		for(VisualBundledTransition t: getBundledTransitions()) {
			if (b.contains(t.getReferencedTransition())) {
				result.add(t);
			}
		}
		return result;
	}

}