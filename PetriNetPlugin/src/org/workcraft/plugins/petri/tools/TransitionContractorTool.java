package org.workcraft.plugins.petri.tools;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Tool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionContractorTool implements Tool {
	private static final int ArrayList = 0;

	@Override
	public String getDisplayName() {
		return "Contract selected transitions";
	}

	@Override
	public String getSection() {
		return "Transformations";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNet;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualPetriNet model = (VisualPetriNet)we.getModelEntry().getVisualModel();
		HashSet<VisualTransition> transitions = new HashSet<VisualTransition>(model.getVisualTransitions());
		if (!model.getSelection().isEmpty()) {
			transitions.retainAll(model.getSelection());
		}
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
				replicatePlace(model, predPlace, succPlace);
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

	private void replicatePlace(VisualPetriNet model, VisualPlace predPlace, VisualPlace succPlace) {
		Container vContainer = (Container)Hierarchy.getCommonParent(predPlace, succPlace);
		Container mContainer = VisualPetriNet.getMathContainer(model, vContainer);

		// Create replica place and put it in the common container
		HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager)model.getPetriNet().getReferenceManager();
		NameManager nameManagerer = refManager.getNameManager((NamespaceProvider)mContainer);
		String predName = model.getPetriNet().getName(predPlace.getReferencedPlace());
		String succName = model.getPetriNet().getName(succPlace.getReferencedPlace());
		String name = nameManagerer.getDerivedName(null, predName + succName);
		VisualPlace place = model.createPlace(name);
		Collection<Node> places = new ArrayList<Node>(Arrays.asList(place));
		model.getRoot().reparent(places, vContainer);

		Point2D pos = Geometry.middle(predPlace.getPosition(), succPlace.getPosition());
		place.setPosition(pos);
		int tokens = predPlace.getReferencedPlace().getTokens() + succPlace.getReferencedPlace().getTokens();
		place.getReferencedPlace().setTokens(tokens);
		for (Node pred: model.getPreset(predPlace)) {
			try {
				model.connect(pred, place);
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}
		for (Node succ: model.getPostset(succPlace)) {
			try {
				model.connect(place, succ);
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}
	}

}
