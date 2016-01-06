package org.workcraft.plugins.petri.tools;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionContractorTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Contract selected transitions";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNet;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualPetriNet model = (VisualPetriNet)we.getModelEntry().getVisualModel();
		HashSet<VisualTransition> transitions = new HashSet<VisualTransition>(model.getVisualTransitions());
		transitions.retainAll(model.getSelection());
		if (!transitions.isEmpty()) {
			we.saveMemento();
			for (VisualTransition transition: transitions) {
				contractTransition(model, transition);
			}
		}
	}

	private void contractTransition(VisualPetriNet model, VisualTransition transition) {
		LinkedList<Node> predNodes = new LinkedList<Node>(model.getPreset(transition));
		LinkedList<Node> succNodes = new LinkedList<Node>(model.getPostset(transition));
		LinkedList<Node> readNodes = new LinkedList<Node>(predNodes);
		readNodes.retainAll(succNodes);
		predNodes.removeAll(readNodes);
		succNodes.removeAll(readNodes);
		// Process read-arcs
		for (Node readNode: readNodes) {
			Connection predConnection = model.getConnection(readNode, transition);
			model.remove(predConnection);
			Connection succConnection = model.getConnection(transition, readNode);
			model.remove(succConnection);
			if (model.getPreset(readNode).isEmpty() && model.getPostset(readNode).isEmpty()) {
				model.remove(readNode);
			}
		}
		// Process producing and consuming arcs
		for (Node predNode: predNodes) {
			VisualPlace predPlace = (VisualPlace)predNode;
			for (Node succNode: succNodes) {
				VisualPlace succPlace = (VisualPlace)succNode;
				replicatePlace(model, transition, predPlace, succPlace);
			}
			Connection predConnection = model.getConnection(predPlace, transition);
			model.remove(predConnection);
		}
		// Clean up
		if (model.getPreset(transition).isEmpty()) {
			model.remove(transition);
		}
		for (Node predNode: predNodes) {
			if (model.getPostset(predNode).isEmpty()) {
				model.remove(predNode);
			}
		}
		for (Node succNode: succNodes) {
			if (model.getPreset(succNode).isEmpty()) {
				model.remove(succNode);
			}
		}
	}

	private void replicatePlace(VisualPetriNet model, VisualTransition transition, VisualPlace predPlace, VisualPlace succPlace) {
		Container vContainer = (Container)Hierarchy.getCommonParent(predPlace, succPlace);
		Container mContainer = NamespaceHelper.getMathContainer(model, vContainer);

		// Create replica place and put it in the common container
		HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager)model.getPetriNet().getReferenceManager();
		NameManager nameManagerer = refManager.getNameManager((NamespaceProvider)mContainer);
		String predName = model.getPetriNet().getName(predPlace.getReferencedPlace());
		String succName = model.getPetriNet().getName(succPlace.getReferencedPlace());
		String name = nameManagerer.getDerivedName(null, predName + succName);
		VisualPlace newPlace = model.createPlace(name, vContainer);

		Point2D pos = Geometry.middle(predPlace.getPosition(), succPlace.getPosition());
		newPlace.setPosition(pos);
		int tokens = predPlace.getReferencedPlace().getTokens() + succPlace.getReferencedPlace().getTokens();
		newPlace.getReferencedPlace().setTokens(tokens);

		for (Connection predConnection: model.getConnections(predPlace)) {
			Node first = predConnection.getFirst();
			Node second = predConnection.getSecond();
			try {
				if (predConnection instanceof VisualReadArc) {
					if ((first instanceof VisualTransition) && (first != transition)) {
						model.connectUndirected(first, newPlace);
					}
					if ((second instanceof VisualTransition) && (second != transition)) {
						model.connectUndirected(newPlace, second);
					}
				} else {
					if ((first instanceof VisualTransition) && (first != transition)) {
						model.connect(first, newPlace);
					}
					if ((second instanceof VisualTransition) && (second!= transition)) {
						model.connect(newPlace, second);
					}
				}
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}

		for (Connection succConnection: model.getConnections(succPlace)) {
			Node first = succConnection.getFirst();
			Node second = succConnection.getSecond();
			try {
				if (succConnection instanceof VisualReadArc) {
					if ((first instanceof VisualTransition) && (first != transition)) {
						model.connectUndirected(first, newPlace);
					}
					if ((second instanceof VisualTransition) && (second != transition)) {
						model.connectUndirected(newPlace, second);
					}
				} else {
					if ((first instanceof VisualTransition) && (first != transition)) {
						model.connect(first, newPlace);
					}
					if ((second instanceof VisualTransition) && (second != transition)) {
						model.connect(newPlace, second);
					}
				}
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}
	}

}
