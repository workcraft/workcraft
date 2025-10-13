package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.exceptions.ImpossibleContractionException;
import org.workcraft.plugins.petri.exceptions.SuspiciousContractionException;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.geom.Point2D;
import java.util.*;

public class ContractTransitionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    public enum ProductPlacePositioning { PRED_PLACE, TRANSITION, SUCC_PLACE, AVERAGE }

    private final Set<VisualConnection> convertedReplicaConnections = new HashSet<>();

    @Override
    public String getDisplayName() {
        return "Contract a selected transition";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Contract transition";
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.APPLICABLE_POPUP_ONLY;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPetri.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualTransition;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void transform(WorkspaceEntry we) {
        VisualModel visualModel = WorkspaceUtils.getAs(we, VisualModel.class);
        Collection<VisualNode> nodes = collectNodes(visualModel);
        if (nodes.size() > 1) {
            DialogUtils.showError("One transition can be contracted at a time.");
        } else if (!nodes.isEmpty()) {
            we.saveMemento();
            transformNodes(visualModel, nodes);
            visualModel.selectNone();
        }
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> transitions = new HashSet<>(Hierarchy.getDescendantsOfType(model.getRoot(), VisualTransition.class));
        transitions.retainAll(model.getSelection());
        return transitions;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualTransition transition) {
            try {
                validateContraction(model, transition);
                removeOrContractTransition(model, transition);
            } catch (SuspiciousContractionException e) {
                contractTransition(model, transition);
                DialogUtils.showWarning(e.getMessage());
            } catch (ImpossibleContractionException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
    }

    public void validateContraction(VisualModel model, VisualTransition transition)
            throws SuspiciousContractionException, ImpossibleContractionException {

        PetriModel mathModel = (PetriModel) model.getMathModel();
        Transition mathTransition = transition.getReferencedComponent();
        if (hasSelfLoopsOnly(mathModel, mathTransition)) {
            return;
        }
        String ref = mathModel.getNodeReference(mathTransition);
        if (hasSelfLoopAndMore(mathModel, mathTransition)) {
            throw new ImpossibleContractionException(
                    "Cannot contract transition " + ref + " with both read-arc (self-loop) and producing/consuming arc.");
        } else if (needsWaitedArcs(mathModel, mathTransition)) {
            throw new ImpossibleContractionException(
                    "Cannot contract transition " + ref + " as it requires weighted arcs that are currently not supported.");
        } else if (isLanguageChanging(mathModel, mathTransition)) {
            throw new SuspiciousContractionException(
                    "Contraction of transition " + ref + " may change the language.");
        } else if (isSafenessViolating(mathModel, mathTransition)) {
            throw new SuspiciousContractionException(
                    "Contraction of transition " + ref + " may be not safeness-preserving.");
        }
    }

    private boolean needsWaitedArcs(PetriModel model, Transition transition) {
        HashSet<MathNode> predPredNodes = new HashSet<>();
        HashSet<MathNode> predSuccNodes = new HashSet<>();
        for (MathNode predNode : model.getPreset(transition)) {
            predPredNodes.addAll(model.getPreset(predNode));
            predSuccNodes.addAll(model.getPostset(predNode));
        }
        HashSet<MathNode> succPredNodes = new HashSet<>();
        HashSet<MathNode> succSuccNodes = new HashSet<>();
        for (MathNode succNode : model.getPostset(transition)) {
            succPredNodes.addAll(model.getPreset(succNode));
            succSuccNodes.addAll(model.getPostset(succNode));
        }
        predPredNodes.retainAll(succPredNodes);
        predSuccNodes.retainAll(succSuccNodes);
        return !(predPredNodes.isEmpty() && predSuccNodes.isEmpty());
    }

    private boolean hasSelfLoopsOnly(PetriModel model, Transition transition) {
        Set<MathNode> preset = model.getPreset(transition);
        Set<MathNode> postset = model.getPostset(transition);
        Set<MathNode> difference = SetUtils.symmetricDifference(preset, postset);
        return difference.isEmpty();
    }

    private boolean hasSelfLoopAndMore(PetriModel model, Transition transition) {
        Set<MathNode> preset = model.getPreset(transition);
        Set<MathNode> postset = model.getPostset(transition);
        Set<MathNode> difference = SetUtils.symmetricDifference(preset, postset);
        Set<MathNode> intersection = SetUtils.intersection(preset, postset);
        return !difference.isEmpty() && !intersection.isEmpty();
    }

    private boolean isLanguageChanging(PetriModel model, Transition transition) {
        return !isType1Secure(model, transition) && !isType2Secure(model, transition);
    }

    // There are no choice places in the preset (preset can be empty).
    private boolean isType1Secure(PetriModel model, Transition transition) {
        Set<MathNode> predNodes = model.getPreset(transition);
        for (MathNode predNode : predNodes) {
            HashSet<MathNode> predSuccNodes = new HashSet<>(model.getPostset(predNode));
            predSuccNodes.remove(transition);
            if (!predSuccNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // There is at least one unmarked place in the postset AND there are no merge places in the postset (the postset cannot be empty).
    private boolean isType2Secure(PetriModel model, Transition transition) {
        Set<MathNode> succNodes = model.getPostset(transition);
        int markedPlaceCount = 0;
        for (MathNode succNode : succNodes) {
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                markedPlaceCount++;
            }
        }
        if (markedPlaceCount >= succNodes.size()) {
            return false;
        }
        for (MathNode succNode : succNodes) {
            HashSet<MathNode> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isSafenessViolating(PetriModel model, Transition transition) {
        return !isType1Safe(model, transition)
                && !isType2Safe(model, transition)
                && !isType3Safe(model, transition);
    }

    // The only place in the postset is unmarked AND it is not a merge.
    private boolean isType1Safe(PetriModel model, Transition transition) {
        Set<MathNode> succNodes = model.getPostset(transition);
        if (succNodes.size() != 1) {
            return false;
        }
        for (MathNode succNode : succNodes) {
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                return false;
            }
            HashSet<MathNode> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // There is a single place in the preset AND all the postset places are unmarked and not merge places (the postset cannot be empty).
    private boolean isType2Safe(PetriModel model, Transition transition) {
        Set<MathNode> predNodes = model.getPreset(transition);
        if (predNodes.size() != 1) {
            return false;
        }
        Set<MathNode> succNodes = model.getPostset(transition);
        if (succNodes.isEmpty()) {
            return false;
        }
        for (MathNode succNode : succNodes) {
            Place succPlace = (Place) succNode;
            if (succPlace.getTokens() != 0) {
                return false;
            }
            HashSet<MathNode> succPredNodes = new HashSet<>(model.getPreset(succNode));
            succPredNodes.remove(transition);
            if (!succPredNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // The only preset place is not a choice.
    private boolean isType3Safe(PetriModel model, Transition transition) {
        Set<MathNode> predNodes = model.getPreset(transition);
        if (predNodes.size() != 1) {
            return false;
        }
        for (MathNode predNode : predNodes) {
            HashSet<MathNode> predSuccNodes = new HashSet<>(model.getPostset(predNode));
            predSuccNodes.remove(transition);
            if (!predSuccNodes.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void removeOrContractTransition(VisualModel model, VisualTransition transition) {
        PetriModel mathModel = (PetriModel) model.getMathModel();
        Transition mathTransition = transition.getReferencedComponent();
        if (hasSelfLoopsOnly(mathModel, mathTransition)) {
            model.remove(transition);
        } else {
            contractTransition(model, transition);
        }
    }

    private void contractTransition(VisualModel model, VisualTransition transition) {
        beforeContraction(model, transition);
        LinkedList<VisualNode> predNodes = new LinkedList<>(model.getPreset(transition));
        LinkedList<VisualNode> succNodes = new LinkedList<>(model.getPostset(transition));
        ProductPlacePositioning productPlacePositioning = getProductPlacePositioning(model, predNodes, succNodes);
        HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaceMap = new HashMap<>();
        for (VisualNode predNode : predNodes) {
            VisualPlace predPlace = (VisualPlace) predNode;
            for (VisualNode succNode : succNodes) {
                VisualPlace succPlace = (VisualPlace) succNode;
                VisualPlace productPlace = createProductPlace(model, predPlace, succPlace);
                productPlaceMap.put(productPlace, new Pair<>(predPlace, succPlace));
                Set<VisualConnection> connections = calcAffectedConnections(model, predPlace, transition, succPlace);
                Map<VisualConnection, VisualConnection> productToOriginalConnectionMap
                        = connectProductPlace(model, connections, productPlace);

                nameProductPlace(model, productPlace, productPlacePositioning, predPlace, succPlace);
                styleProductPlace(productPlace, productPlacePositioning, predPlace, succPlace);
                positionProductPlace(model, productPlace, productPlacePositioning, predPlace, transition, succPlace);

                shapeProductPredConnections(model, productToOriginalConnectionMap,
                        productPlace, productPlacePositioning, predPlace, transition);

                shapeProductSuccConnections(model, productToOriginalConnectionMap,
                        productPlace, productPlacePositioning, transition, succPlace);
            }
        }
        model.remove(transition);
        afterContraction(model, productPlaceMap);
        for (VisualNode predNode : predNodes) {
            model.remove(predNode);
        }
        for (VisualNode succNode : succNodes) {
            model.remove(succNode);
        }
    }

    private static HashSet<VisualConnection> calcAffectedConnections(VisualModel model,
            VisualPlace predPlace, VisualTransition transition, VisualPlace succPlace) {

        HashSet<VisualConnection> connections = new HashSet<>();
        connections.addAll(model.getConnections(predPlace));
        connections.addAll(model.getConnections(succPlace));
        connections.removeAll(model.getConnections(transition));
        return connections;
    }

    private static ProductPlacePositioning getProductPlacePositioning(VisualModel model,
            LinkedList<VisualNode> predNodes, LinkedList<VisualNode> succNodes) {

        ProductPlacePositioning productPlacePositioning = ProductPlacePositioning.TRANSITION;
        if ((predNodes.size() > 1) && (succNodes.size() > 1)) {
            productPlacePositioning = ProductPlacePositioning.AVERAGE;
        } else if (predNodes.size() > 1) {
            productPlacePositioning = ProductPlacePositioning.PRED_PLACE;
        } else if (succNodes.size() > 1) {
            productPlacePositioning = ProductPlacePositioning.SUCC_PLACE;
        } else if (!predNodes.isEmpty() && !succNodes.isEmpty()) {
            Set<VisualNode> succPredNodes = model.getPostset(predNodes.iterator().next());
            Set<VisualNode> predSuccNodes = model.getPreset(succNodes.iterator().next());
            if ((succPredNodes.size() > 1) && (predSuccNodes.size() == 1)) {
                productPlacePositioning = ProductPlacePositioning.PRED_PLACE;
            } else if ((succPredNodes.size() == 1) && (predSuccNodes.size() > 1)) {
                productPlacePositioning = ProductPlacePositioning.SUCC_PLACE;
            }
        }
        return productPlacePositioning;
    }

    public VisualPlace createProductPlace(VisualModel model, VisualPlace predPlace, VisualPlace succPlace) {
        Container visualContainer = (Container) Hierarchy.getCommonParent(predPlace, succPlace);
        Container mathContainer = NamespaceHelper.getMathContainer(model, visualContainer);
        MathModel mathModel = model.getMathModel();
        Place mathPlace = mathModel.createNode(null, mathContainer, Place.class);
        return model.createVisualComponent(mathPlace, VisualPlace.class, visualContainer);
    }

    private Map<VisualConnection, VisualConnection> connectProductPlace(VisualModel visualModel,
            Set<VisualConnection> originalConnections, VisualPlace productPlace) {

        Map<VisualConnection, VisualConnection> productConnectionMap = new HashMap<>();
        for (VisualConnection originalConnection : originalConnections) {
            VisualNode first = originalConnection.getFirst();
            VisualNode second = originalConnection.getSecond();
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
                LogUtils.logWarning(e.getMessage());
            }
            if (newConnection != null) {
                productConnectionMap.put(newConnection, originalConnection);
                newConnection.copyStyle(originalConnection);
                newConnection.copyShape(originalConnection);
                filterControlPoints(newConnection);
                if (convertedReplicaConnections.contains(originalConnection)) {
                    convertedReplicaConnections.add(newConnection);
                }
            }
        }
        return productConnectionMap;
    }

    public void nameProductPlace(VisualModel model, VisualPlace productPlace,
            ProductPlacePositioning productPlacePositioning, VisualPlace predPlace, VisualPlace succPlace) {

        switch (productPlacePositioning) {
            case PRED_PLACE -> ModelUtils.setNameRenameClashes(model, productPlace, model.getMathName(predPlace));
            case SUCC_PLACE -> ModelUtils.setNameRenameClashes(model, productPlace, model.getMathName(succPlace));
            default -> {
                String mixName = model.getMathName(predPlace) + model.getMathName(succPlace);
                ModelUtils.setNameRenameClashes(model, productPlace, mixName);
            }
        }
    }

    public void styleProductPlace(VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualPlace predPlace, VisualPlace succPlace) {

        switch (productPlacePositioning) {
            case PRED_PLACE -> productPlace.copyStyle(predPlace);
            case SUCC_PLACE -> productPlace.copyStyle(succPlace);
            default -> productPlace.mixStyle(predPlace, succPlace);
        }
        // Correct the token count and capacity of the new place
        Place mathPredPlace = predPlace.getReferencedComponent();
        Place mathSuccPlace = succPlace.getReferencedComponent();
        Place mathProductPlace = productPlace.getReferencedComponent();
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

    public void positionProductPlace(VisualModel model,
            VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualPlace predPlace, VisualTransition transition, VisualPlace succPlace) {

        switch (productPlacePositioning) {
            case PRED_PLACE -> productPlace.copyPosition(predPlace);
            case TRANSITION -> productPlace.copyPosition(transition);
            case SUCC_PLACE -> productPlace.copyPosition(succPlace);
            default -> {
                Point2D mixPosition = Geometry.middle(predPlace.getRootSpacePosition(), succPlace.getRootSpacePosition());
                productPlace.setRootSpacePosition(mixPosition);
            }
        }
    }

    public void shapeProductPredConnections(VisualModel model,
            Map<VisualConnection, VisualConnection> productToOriginalConnectionMap,
            VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualPlace predPlace, VisualTransition transition) {

        if ((productPlacePositioning == ProductPlacePositioning.TRANSITION)
                && (model.getPreset(predPlace).size() < 2)) {

            VisualConnection predConnection = model.getConnection(predPlace, transition);
            LinkedList<Point2D> predLocations = ConnectionHelper.getMergedControlPoints(
                    predPlace, null, predConnection);

            for (VisualConnection newConnection : productToOriginalConnectionMap.keySet()) {
                if (newConnection.getSecond() == productPlace) {
                    ConnectionHelper.addControlPoints(newConnection, predLocations);
                }
                filterControlPoints(newConnection);
            }
        }
    }

    public void shapeProductSuccConnections(VisualModel model,
            Map<VisualConnection, VisualConnection> productToOriginalConnectionMap,
            VisualPlace productPlace, ProductPlacePositioning productPlacePositioning,
            VisualTransition transition, VisualPlace succPlace) {

        if ((productPlacePositioning == ProductPlacePositioning.TRANSITION)
                && (model.getPostset(succPlace).size() < 2)) {

            VisualConnection succConnection = model.getConnection(transition, succPlace);
            LinkedList<Point2D> succLocations = ConnectionHelper.getMergedControlPoints(
                    succPlace, succConnection, null);

            for (VisualConnection newConnection : productToOriginalConnectionMap.keySet()) {
                if (newConnection.getFirst() == productPlace) {
                    ConnectionHelper.prependControlPoints(newConnection, succLocations);
                }
                filterControlPoints(newConnection);
            }
        }
    }

    public void beforeContraction(VisualModel visualModel, VisualTransition visualTransition) {
        Set<Place> affectedPlaces = new HashSet<>();
        for (VisualConnection connection : visualModel.getConnections(visualTransition)) {
            VisualNode otherNode = connection.getFirst() == visualTransition ? connection.getSecond() : connection.getFirst();
            if (otherNode instanceof VisualReplicaPlace visualReplicaPlace) {
                affectedPlaces.add(visualReplicaPlace.getReferencedComponent());
            } else if (otherNode instanceof VisualPlace visualPlace) {
                affectedPlaces.add(visualPlace.getReferencedComponent());
            }
        }
        convertedReplicaConnections.clear();
        for (VisualReplicaPlace visualReplicaPlace : ConnectionUtils.getVisualReplicaPlaces(visualModel)) {
            if (affectedPlaces.contains(visualReplicaPlace.getReferencedComponent())) {
                Set<VisualConnection> newConnections = ConversionUtils.collapseReplicaPlace(visualModel, visualReplicaPlace);
                convertedReplicaConnections.addAll(newConnections);
            }
        }
    }

    public void afterContraction(VisualModel model,
            HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaceMap) {

        Set<VisualConnection> replicaPlaceConnections = new HashSet<>();
        for (VisualPlace productPlace : productPlaceMap.keySet()) {
            Pair<VisualPlace, VisualPlace> originalPlaces = productPlaceMap.get(productPlace);
            VisualPlace predPlace = originalPlaces.getFirst();
            VisualPlace succPlace = originalPlaces.getSecond();
            for (VisualConnection productConnection : model.getConnections(productPlace)) {
                Connection predPlaceConnection = null;
                Connection succPlaceConnection = null;
                if (productConnection.getFirst() instanceof VisualTransition productPredTransition) {
                    predPlaceConnection = model.getConnection(productPredTransition, predPlace);
                    succPlaceConnection = model.getConnection(productPredTransition, succPlace);
                }
                if (productConnection.getSecond() instanceof VisualTransition productSuccTransition) {
                    predPlaceConnection = model.getConnection(predPlace, productSuccTransition);
                    succPlaceConnection = model.getConnection(succPlace, productSuccTransition);
                }
                if (((predPlaceConnection == null) || convertedReplicaConnections.contains(predPlaceConnection))
                        && ((succPlaceConnection == null) || convertedReplicaConnections.contains(succPlaceConnection))) {

                    replicaPlaceConnections.add(productConnection);
                }
            }
        }
        for (VisualConnection replicaPlaceConnection : replicaPlaceConnections) {
            ConversionUtils.replicateConnectedPlace(model, replicaPlaceConnection);
        }
    }

    public void filterControlPoints(VisualConnection connection) {
        if (connection != null) {
            ConnectionGraphic graphic = connection.getGraphic();
            if (graphic instanceof Polyline) {
                ConnectionHelper.filterControlPoints((Polyline) graphic);
            }
        }
    }

}
