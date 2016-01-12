package org.workcraft.plugins.petri.tools;

import java.awt.geom.Point2D;
import java.util.HashMap;
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
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionContractorTool extends TransformationTool {

	private static final String MESSAGE_TITLE = "Transition contraction";

	private HashSet<VisualConnection> replicaPlaces = new HashSet<>();

	@Override
	public String getDisplayName() {
		return "Contract selected transitions";
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
		final VisualModel visualModel = we.getModelEntry().getVisualModel();
		HashSet<VisualTransition> visualTransitions = PetriNetUtils.getVisualTransitions(visualModel);
		visualTransitions.retainAll(visualModel.getSelection());
		if (visualTransitions.size() > 1) {
			JOptionPane.showMessageDialog(null, "One transition can be contracted at a time.", MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE);
		} else if (!visualTransitions.isEmpty()) {
			we.saveMemento();
			PetriNetModel mathModel = (PetriNetModel)visualModel.getMathModel();
			for (VisualTransition visualTransition: visualTransitions) {
				Transition mathTransition = visualTransition.getReferencedTransition();
				if (hasSelfLoop(mathModel, mathTransition)) {
					JOptionPane.showMessageDialog(null, "Error: a transition with a self-loop/read-arc cannot be contracted.", MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE);
				} else if (isLanguageChanging(mathModel, mathTransition)) {
					contractTransition(visualModel, visualTransition);
					JOptionPane.showMessageDialog(null, "Warning: this transformation may change the language.", MESSAGE_TITLE, JOptionPane.WARNING_MESSAGE);
				} else if (isSafenessViolationg(mathModel, mathTransition)) {
					contractTransition(visualModel, visualTransition);
					JOptionPane.showMessageDialog(null, "Warning: this transformation may be not safeness-preserving.", MESSAGE_TITLE, JOptionPane.WARNING_MESSAGE);
				} else {
					contractTransition(visualModel, visualTransition);
				}
			}
		}
	}

	private boolean hasSelfLoop(PetriNetModel model, Transition transition) {
		HashSet<Node> connectedNodes = new HashSet<>(model.getPreset(transition));
		connectedNodes.retainAll(model.getPostset(transition));
		return !connectedNodes.isEmpty();
	}

	private boolean isLanguageChanging(PetriNetModel model, Transition transition) {
		return ( !isType1Secure(model, transition) && !isType2Secure(model, transition) );
	}

	// There are no choice places in the preset (preset can be empty).
	private boolean isType1Secure(PetriNetModel model, Transition transition) {
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
	private boolean isType2Secure(PetriNetModel model, Transition transition) {
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

	private boolean isSafenessViolationg(PetriNetModel model, Transition transition) {
		return ( !isType1Safe(model, transition) && !isType2Safe(model, transition) && !isType3Safe(model, transition));
	}

	// The only place in the postset is unmarked AND it is not a merge.
	private boolean isType1Safe(PetriNetModel model, Transition transition) {
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
	private boolean isType2Safe(PetriNetModel model, Transition transition) {
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
	private boolean isType3Safe(PetriNetModel model, Transition transition) {
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

	private void contractTransition(VisualModel visualModel, VisualTransition visualTransition) {
		beforeContraction(visualModel, visualTransition);
		LinkedList<Node> predNodes = new LinkedList<Node>(visualModel.getPreset(visualTransition));
		LinkedList<Node> succNodes = new LinkedList<Node>(visualModel.getPostset(visualTransition));
		HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaces = new HashMap<>();
		for (Node predNode: predNodes) {
			VisualPlace predPlace = (VisualPlace)predNode;
			for (Node succNode: succNodes) {
				VisualPlace succPlace = (VisualPlace)succNode;
				VisualPlace productPlace = createProductPlace(visualModel, predPlace, succPlace);
				initialiseProductPlace(visualModel, predPlace, succPlace, productPlace);

				HashSet<Connection> connections = new HashSet<>();
				connections.addAll(visualModel.getConnections(predPlace));
				connections.addAll(visualModel.getConnections(succPlace));
				connectProductPlace(visualModel, connections, productPlace);
				productPlaces.put(productPlace, new Pair<>(predPlace, succPlace));
			}
		}
		visualModel.remove(visualTransition);
		for (Node predNode: predNodes) {
			visualModel.remove(predNode);
		}
		for (Node succNode: succNodes) {
			visualModel.remove(succNode);
		}
		afterContraction(visualModel, visualTransition, productPlaces);
	}

	public void beforeContraction(VisualModel visualModel, VisualTransition visualTransition) {
		replicaPlaces.clear();
		Set<Connection> adjacentConnections = new HashSet<>(visualModel.getConnections(visualTransition));
		for (Connection connection: adjacentConnections) {
			VisualReplicaPlace replica = null;
			if (connection.getFirst() instanceof VisualReplicaPlace) {
				replica = (VisualReplicaPlace)connection.getFirst();
			}
			if (connection.getSecond() instanceof VisualReplicaPlace) {
				replica = (VisualReplicaPlace)connection.getSecond();
			}
			if (replica != null) {
				VisualConnection newConnection = PetriNetUtils.collapseReplicaPlace(visualModel, replica);
				replicaPlaces.add(newConnection);
			}
		}
	}

	public void afterContraction(VisualModel visualModel, VisualTransition visualTransition,
			HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaces) {
	}

	public VisualPlace createProductPlace(VisualModel visualModel, VisualPlace predPlace, VisualPlace succPlace) {
		Container visualContainer = (Container)Hierarchy.getCommonParent(predPlace, succPlace);
		Container mathContainer = NamespaceHelper.getMathContainer(visualModel, visualContainer);
		MathModel mathModel = visualModel.getMathModel();
		HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager)mathModel.getReferenceManager();
		NameManager nameManagerer = refManager.getNameManager((NamespaceProvider)mathContainer);
		String predName = visualModel.getMathName(predPlace);
		String succName = visualModel.getMathName(succPlace);
		String productName = nameManagerer.getDerivedName(null, predName + succName);
		Place mathPlace = mathModel.createNode(productName, mathContainer, Place.class);
		return visualModel.createVisualComponent(mathPlace, visualContainer, VisualPlace.class);
	}

	public void initialiseProductPlace(VisualModel visualModel, VisualPlace predPlace, VisualPlace succPlace, VisualPlace productPlace) {
		Point2D pos = Geometry.middle(predPlace.getRootSpacePosition(), succPlace.getRootSpacePosition());
		productPlace.setRootSpacePosition(pos);
		productPlace.mixStyle(predPlace, succPlace);
		// Correct the token count and capacity of the new place
		Place mathPredPlace = predPlace.getReferencedPlace();
		Place mathSuccPlace = succPlace.getReferencedPlace();
		Place mathProductPlace = productPlace.getReferencedPlace();
		int tokens = mathPredPlace.getTokens() + mathSuccPlace.getTokens();
		mathProductPlace.setTokens(tokens);
		int capacity = tokens;
		if (capacity < mathPredPlace.getCapacity()) {
			capacity = mathPredPlace.getCapacity();
		}
		if (capacity < mathSuccPlace.getCapacity()) {
			capacity = mathSuccPlace.getCapacity();
		}
		mathProductPlace.setCapacity(capacity);
	}

	public Set<Connection> connectProductPlace(VisualModel visualModel, Set<Connection> originalConnections, VisualPlace productPlace) {
		HashSet<Connection> newConnections = new HashSet<>();
		for (Connection originalConnection: originalConnections) {
			Node first = originalConnection.getFirst();
			Node second = originalConnection.getSecond();
			VisualConnection newConnection = null;
			try {
				if (originalConnection instanceof VisualReadArc) {
					if (first instanceof VisualTransition) {
						newConnection = visualModel.connectUndirected(first, productPlace);
					}
					if (second instanceof VisualTransition) {
						newConnection = visualModel.connectUndirected(productPlace, second);
					}
				} else {
					if (first instanceof VisualTransition) {
						newConnection = visualModel.connect(first, productPlace);
					}
					if (second instanceof VisualTransition) {
						newConnection = visualModel.connect(productPlace, second);
					}
				}
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
			if (newConnection != null) {
				newConnections.add(newConnection);
				if (originalConnection instanceof VisualConnection) {
					newConnection.copyStyle((VisualConnection)originalConnection);
					newConnection.copyShape((VisualConnection)originalConnection);
				}
			}
		}
		return newConnections;
	}

}
