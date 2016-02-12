package org.workcraft.plugins.petri.tools;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.Replica;
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

public class TransitionContractorTool extends TransformationTool implements NodeTransformer {

    private static final String MESSAGE_TITLE = "Transition contraction";

    private HashSet<VisualConnection> convertedReplicaConnections = new HashSet<>();

    @Override
    public String getDisplayName() {
        return "Contract a selected transition";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof PetriNet;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualTransition;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualModel visualModel = we.getModelEntry().getVisualModel();
        HashSet<VisualTransition> visualTransitions = PetriNetUtils.getVisualTransitions(visualModel);
        visualTransitions.retainAll(visualModel.getSelection());
        if (visualTransitions.size() > 1) {
            JOptionPane.showMessageDialog(null,
                    "One transition can be contracted at a time.",
                    MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE);
        } else if (!visualTransitions.isEmpty()) {
            we.saveMemento();
            for (VisualTransition visualTransition: visualTransitions) {
                transform(visualModel, visualTransition);
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualTransition)) {
            VisualModel visualModel = (VisualModel) model;
            PetriNetModel mathModel = (PetriNetModel) visualModel.getMathModel();
            VisualTransition visualTransition = (VisualTransition) node;
            Transition mathTransition = visualTransition.getReferencedTransition();
            if (hasSelfLoop(mathModel, mathTransition)) {
                JOptionPane.showMessageDialog(null,
                        "Error: a transition with a self-loop/read-arc cannot be contracted.",
                        MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE);
            } else if (needsWaitedArcs(mathModel, mathTransition)) {
                JOptionPane.showMessageDialog(null,
                        "Error: this transformation requires weighted arcs that are currently not supported.",
                        MESSAGE_TITLE, JOptionPane.ERROR_MESSAGE);
            } else if (isLanguageChanging(mathModel, mathTransition)) {
                contractTransition(visualModel, visualTransition);
                JOptionPane.showMessageDialog(null,
                        "Warning: this transformation may change the language.",
                        MESSAGE_TITLE, JOptionPane.WARNING_MESSAGE);
            } else if (isSafenessViolationg(mathModel, mathTransition)) {
                contractTransition(visualModel, visualTransition);
                JOptionPane.showMessageDialog(null,
                        "Warning: this transformation may be not safeness-preserving.",
                        MESSAGE_TITLE, JOptionPane.WARNING_MESSAGE);
            } else {
                contractTransition(visualModel, visualTransition);
            }
        }
    }

    private boolean needsWaitedArcs(PetriNetModel model, Transition transition) {
        HashSet<Node> predPredNodes = new HashSet<>();
        HashSet<Node> predSuccNodes = new HashSet<>();
        for (Node predNode: model.getPreset(transition)) {
            predPredNodes.addAll(model.getPreset(predNode));
            predSuccNodes.addAll(model.getPostset(predNode));
        }
        HashSet<Node> succPredNodes = new HashSet<>();
        HashSet<Node> succSuccNodes = new HashSet<>();
        for (Node succNode: model.getPostset(transition)) {
            succPredNodes.addAll(model.getPreset(succNode));
            succSuccNodes.addAll(model.getPostset(succNode));
        }
        predPredNodes.retainAll(succPredNodes);
        predSuccNodes.retainAll(succSuccNodes);
        return !(predPredNodes.isEmpty() && predSuccNodes.isEmpty());
    }

    private boolean hasSelfLoop(PetriNetModel model, Transition transition) {
        HashSet<Node> connectedNodes = new HashSet<>(model.getPreset(transition));
        connectedNodes.retainAll(model.getPostset(transition));
        return !connectedNodes.isEmpty();
    }

    private boolean isLanguageChanging(PetriNetModel model, Transition transition) {
        return !isType1Secure(model, transition) && !isType2Secure(model, transition);
    }

    // There are no choice places in the preset (preset can be empty).
    private boolean isType1Secure(PetriNetModel model, Transition transition) {
        Set<Node> predNodes = model.getPreset(transition);
        for (Node predNode: predNodes) {
            HashSet<Node> predSuccNodes = new HashSet<>(model.getPostset(predNode));
            predSuccNodes.remove(transition);
            if (!predSuccNodes.isEmpty()) {
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
            Place succPlace = (Place) succNode;
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
            if (!succPredNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isSafenessViolationg(PetriNetModel model, Transition transition) {
        return !isType1Safe(model, transition) && !isType2Safe(model, transition) && !isType3Safe(model, transition);
    }

    // The only place in the postset is unmarked AND it is not a merge.
    private boolean isType1Safe(PetriNetModel model, Transition transition) {
        Set<Node> succNodes = model.getPostset(transition);
        if (succNodes.size() != 1) {
            return false;
        }
        for (Node succNode: succNodes) {
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                return false;
            }
            HashSet<Node> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
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
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                return false;
            }
            HashSet<Node> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
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
            if (!predSuccNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void contractTransition(VisualModel visualModel, VisualTransition visualTransition) {
        beforeContraction(visualModel, visualTransition);
        LinkedList<Node> predNodes = new LinkedList<Node>(visualModel.getPreset(visualTransition));
        LinkedList<Node> succNodes = new LinkedList<Node>(visualModel.getPostset(visualTransition));
        HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaceMap = new HashMap<>();
        for (Node predNode: predNodes) {
            VisualPlace predPlace = (VisualPlace) predNode;
            for (Node succNode: succNodes) {
                VisualPlace succPlace = (VisualPlace) succNode;
                VisualPlace productPlace = createProductPlace(visualModel, predPlace, succPlace);
                initialiseProductPlace(visualModel, predPlace, succPlace, productPlace);

                HashSet<Connection> connections = new HashSet<>();
                connections.addAll(visualModel.getConnections(predPlace));
                connections.addAll(visualModel.getConnections(succPlace));
                connectProductPlace(visualModel, connections, productPlace);
                productPlaceMap.put(productPlace, new Pair<>(predPlace, succPlace));
            }
        }
        visualModel.remove(visualTransition);
        afterContraction(visualModel, visualTransition, productPlaceMap);
        for (Node predNode: predNodes) {
            visualModel.remove(predNode);
        }
        for (Node succNode: succNodes) {
            visualModel.remove(succNode);
        }
    }

    public VisualPlace createProductPlace(VisualModel visualModel, VisualPlace predPlace, VisualPlace succPlace) {
        Container visualContainer = (Container) Hierarchy.getCommonParent(predPlace, succPlace);
        Container mathContainer = NamespaceHelper.getMathContainer(visualModel, visualContainer);
        MathModel mathModel = visualModel.getMathModel();
        HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager) mathModel.getReferenceManager();
        NameManager nameManagerer = refManager.getNameManager((NamespaceProvider) mathContainer);
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

    public HashMap<Connection, Connection> connectProductPlace(VisualModel visualModel, Set<Connection> originalConnections, VisualPlace productPlace) {
        HashMap<Connection, Connection> productConnectionMap = new HashMap<>();
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
                //e.printStackTrace();
            }
            if ((newConnection != null) && (originalConnection instanceof VisualConnection)) {
                productConnectionMap.put(newConnection, originalConnection);
                newConnection.copyStyle((VisualConnection) originalConnection);
                newConnection.copyShape((VisualConnection) originalConnection);
            }
        }
        return productConnectionMap;
    }

    public void beforeContraction(VisualModel visualModel, VisualTransition visualTransition) {
        convertedReplicaConnections.clear();
        HashSet<VisualReplicaPlace> replicaPlaces = new HashSet<>();
        for (Connection connection: visualModel.getConnections(visualTransition)) {
            VisualReplicaPlace replicaPlace = null;
            if (connection.getFirst() instanceof VisualReplicaPlace) {
                replicaPlace = (VisualReplicaPlace) connection.getFirst();
            }
            if (connection.getSecond() instanceof VisualReplicaPlace) {
                replicaPlace = (VisualReplicaPlace) connection.getSecond();
            }
            if (replicaPlace != null) {
                replicaPlaces.add(replicaPlace);
            }

            VisualPlace place = null;
            if (connection.getFirst() instanceof VisualPlace) {
                place = (VisualPlace) connection.getFirst();
            }
            if (connection.getSecond() instanceof VisualPlace) {
                place = (VisualPlace) connection.getSecond();
            }
            if (place != null) {
                for (Replica replica: place.getReplicas()) {
                    if (replica instanceof VisualReplicaPlace) {
                        replicaPlaces.add((VisualReplicaPlace) replica);
                    }
                }
            }
        }
        for (VisualReplicaPlace replica: replicaPlaces) {
            VisualConnection newConnection = PetriNetUtils.collapseReplicaPlace(visualModel, replica);
            convertedReplicaConnections.add(newConnection);
        }
    }

    public void afterContraction(VisualModel visualModel, VisualTransition visualTransition,
            HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaceMap) {
        HashSet<VisualConnection> replicaPlaceConnections = new HashSet<>();
        for (VisualPlace productPlace: productPlaceMap.keySet()) {
            for (Connection productConnection: visualModel.getConnections(productPlace)) {
                Pair<VisualPlace, VisualPlace> originalPlaces = productPlaceMap.get(productPlace);
                VisualPlace predPlace = originalPlaces.getFirst();
                VisualPlace succPlace = originalPlaces.getSecond();

                Connection predPlaceConnection = null;
                Connection succPlaceConnection = null;
                if (productConnection.getFirst() instanceof VisualTransition) {
                    VisualTransition transition = (VisualTransition) productConnection.getFirst();
                    predPlaceConnection = visualModel.getConnection(transition, predPlace);
                    succPlaceConnection = visualModel.getConnection(transition, succPlace);
                }
                if (productConnection.getSecond() instanceof VisualTransition) {
                    VisualTransition transition = (VisualTransition) productConnection.getSecond();
                    predPlaceConnection = visualModel.getConnection(predPlace, transition);
                    succPlaceConnection = visualModel.getConnection(succPlace, transition);
                }
                if (((predPlaceConnection == null) || convertedReplicaConnections.contains(predPlaceConnection))
                        &&((succPlaceConnection == null) || convertedReplicaConnections.contains(succPlaceConnection))) {
                    replicaPlaceConnections.add((VisualConnection) productConnection);
                }
            }
        }
        for (VisualConnection replicaPlaceConnection: replicaPlaceConnections) {
            PetriNetUtils.replicateConnectedPlace(visualModel, replicaPlaceConnection);
        }
    }

}
