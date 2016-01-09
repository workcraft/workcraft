package org.workcraft.plugins.petri.tools;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionContractorTool extends TransformationTool {
	private static final String title = "Transition contraction";

	@Override
	public String getDisplayName() {
		return "Contract selected transition";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNet;
	}

	@Override
	public boolean isApplicableToNode(Node node) {
		return (node instanceof VisualTransition);
	};

	@Override
	public void run(WorkspaceEntry we) {
		final VisualPetriNet model = (VisualPetriNet)we.getModelEntry().getVisualModel();
		HashSet<VisualTransition> transitions = new HashSet<VisualTransition>(model.getVisualTransitions());
		transitions.retainAll(model.getSelection());
		if (transitions.size() > 1) {
			JOptionPane.showMessageDialog(null, "One transition can be contracted at a time.", title, JOptionPane.WARNING_MESSAGE);
		} else if (!transitions.isEmpty()) {
			we.saveMemento();
			for (VisualTransition transition: transitions) {
				if (hasSelfLoop(model.getPetriNet(), transition.getReferencedTransition())) {
					JOptionPane.showMessageDialog(null, "A transition with a self-loop/read-arc cannot be contracted.", title, JOptionPane.ERROR_MESSAGE);
				} else if (isLanguageChanging(model.getPetriNet(), transition.getReferencedTransition())) {
					contractTransition(model, transition);
					JOptionPane.showMessageDialog(null, "This transforLanguage can be changed.", title, JOptionPane.WARNING_MESSAGE);
				} else if (isSafenessViolationg(model.getPetriNet(), transition.getReferencedTransition())) {
					contractTransition(model, transition);
					JOptionPane.showMessageDialog(null, "Safeness can be violated.", title, JOptionPane.WARNING_MESSAGE);
				} else {
					contractTransition(model, transition);
				}
			}
		}
	}

	private static boolean hasSelfLoop(PetriNet model, Transition transition) {
		HashSet<Node> connectedNodes = new HashSet<>(model.getPreset(transition));
		connectedNodes.retainAll(model.getPostset(transition));
		return !connectedNodes.isEmpty();
	}

	private static boolean isLanguageChanging(PetriNet model, Transition transition) {
		return ( !isType1Secure(model, transition) && !isType2Secure(model, transition) );
	}

	// There are no choice places in the preset (preset can be empty).
	private static boolean isType1Secure(PetriNet model, Transition transition) {
		Set<Node> predNodes = model.getPreset(transition);
		for (Node predNode: predNodes) {
			HashSet<Node> predSuccNodes = new HashSet<>(model.getPostset(predNode));
			predSuccNodes.remove(transition);
			if ( !predSuccNodes.isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	// There is at least one unmarked place in the postset AND there are no merge places in the postset (the postset cannot be empty).
	private static boolean isType2Secure(PetriNet model, Transition transition) {
		Set<Node> succNodes = model.getPostset(transition);
		int markedPlaceCount = 0;
		for (Node succNode: succNodes) {
			Place succPlace = (Place)succNode;
			if (succPlace.getTokens() != 0) {
				markedPlaceCount++;
			}
		}
		if (markedPlaceCount >= succNodes.size()) {
			return false;
		}
		for (Node succNode: succNodes) {
			HashSet<Node> succPredNodes = new HashSet<>(model.getPreset(succNode));
			succPredNodes.remove(transition);
			if ( !succPredNodes.isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	private static boolean isSafenessViolationg(PetriNet model, Transition transition) {
		return ( !isType1Safe(model, transition) && !isType2Safe(model, transition) && !isType3Safe(model, transition));
	}

	// The only place in the postset is unmarked AND it is not a merge.
	private static boolean isType1Safe(PetriNet model, Transition transition) {
		Set<Node> succNodes = model.getPostset(transition);
		if (succNodes.size() != 1) {
			return false;
		}
		for (Node succNode: succNodes) {
			Place succPlace = (Place)succNode;
			if (succPlace.getTokens() != 0) {
				return false;
			}
			HashSet<Node> succPredNodes = new HashSet<>(model.getPreset(succNode));
			succPredNodes.remove(transition);
			if ( !succPredNodes.isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	// There is a single place in the preset AND all the postset places are unmarked and not merge places (the postset cannot be empty).
	private static boolean isType2Safe(PetriNet model, Transition transition) {
		Set<Node> predNodes = model.getPreset(transition);
		if (predNodes.size() != 1) {
			return false;
		}
		Set<Node> succNodes = model.getPostset(transition);
		if (succNodes.isEmpty()) {
			return false;
		}
		for (Node succNode: succNodes) {
			Place succPlace = (Place)succNode;
			if (succPlace.getTokens() != 0) {
				return false;
			}
			HashSet<Node> succPredNodes = new HashSet<>(model.getPreset(succNode));
			succPredNodes.remove(transition);
			if ( !succPredNodes.isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	// The only preset place is not a choice.
	private static boolean isType3Safe(PetriNet model, Transition transition) {
		Set<Node> predNodes = model.getPreset(transition);
		if (predNodes.size() != 1) {
			return false;
		}
		for (Node predNode: predNodes) {
			HashSet<Node> predSuccNodes = new HashSet<>(model.getPostset(predNode));
			predSuccNodes.remove(transition);
			if ( !predSuccNodes.isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	private static void contractTransition(VisualPetriNet model, VisualTransition transition) {
		LinkedList<Node> predNodes = new LinkedList<Node>(model.getPreset(transition));
		LinkedList<Node> succNodes = new LinkedList<Node>(model.getPostset(transition));
		for (Node predNode: predNodes) {
			VisualPlace predPlace = (VisualPlace)predNode;
			for (Node succNode: succNodes) {
				VisualPlace succPlace = (VisualPlace)succNode;
				replicatePlace(model, predPlace, succPlace);
			}
		}
		model.remove(transition);
		for (Node predNode: predNodes) {
			model.remove(predNode);
		}
		for (Node succNode: succNodes) {
			model.remove(succNode);
		}
	}

	private static void replicatePlace(VisualPetriNet model, VisualPlace predPlace, VisualPlace succPlace) {
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
		newPlace.mixStyle(predPlace, succPlace);
		// Accumulate token and capacity count
		int tokens = predPlace.getReferencedPlace().getTokens() + succPlace.getReferencedPlace().getTokens();
		newPlace.getReferencedPlace().setTokens(tokens);
		int capacity= predPlace.getReferencedPlace().getCapacity() + succPlace.getReferencedPlace().getCapacity();
		newPlace.getReferencedPlace().setCapacity(capacity);

		for (Connection predConnection: model.getConnections(predPlace)) {
			Node first = predConnection.getFirst();
			Node second = predConnection.getSecond();
			VisualConnection newConnection = null;
			try {
				if (predConnection instanceof VisualReadArc) {
					if (first instanceof VisualTransition) {
						newConnection = model.connectUndirected(first, newPlace);
					}
					if (second instanceof VisualTransition) {
						newConnection = model.connectUndirected(newPlace, second);
					}
				} else {
					if (first instanceof VisualTransition) {
						newConnection = model.connect(first, newPlace);
					}
					if (second instanceof VisualTransition) {
						newConnection = model.connect(newPlace, second);
					}
				}
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
			if ((newConnection != null) && (predConnection instanceof VisualConnection)) {
				newConnection.copyStyle((VisualConnection)predConnection);
				newConnection.copyShape((VisualConnection)predConnection);
			}
		}

		for (Connection succConnection: model.getConnections(succPlace)) {
			Node first = succConnection.getFirst();
			Node second = succConnection.getSecond();
			VisualConnection newConnection = null;
			try {
				if (succConnection instanceof VisualReadArc) {
					if (first instanceof VisualTransition) {
						newConnection = model.connectUndirected(first, newPlace);
					}
					if (second instanceof VisualTransition) {
						newConnection = model.connectUndirected(newPlace, second);
					}
				} else {
					if (first instanceof VisualTransition) {
						newConnection = model.connect(first, newPlace);
					}
					if (second instanceof VisualTransition) {
						newConnection = model.connect(newPlace, second);
					}
				}
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
			if ((newConnection != null) && (succConnection instanceof VisualConnection)) {
				newConnection.copyStyle((VisualConnection)succConnection);
				newConnection.copyShape((VisualConnection)succConnection);
			}

		}
	}

}
