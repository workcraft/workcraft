package org.workcraft.plugins.policy.converters;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.policy.*;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PolicyToPetriConverter {

    private final VisualPolicy policyNet;
    private final VisualPetri petriNet;
    private final Map<VisualPlace, VisualPlace> placeMap;
    private final Map<VisualBundledTransition, VisualTransition> transitionMap;
    private final Map<VisualBundle, VisualTransition> bundleMap;

    public PolicyToPetriConverter(VisualPolicy policyNet) {
        this.policyNet = policyNet;
        this.petriNet = new VisualPetri(new Petri());
        convertTitle();
        placeMap = convertPlaces();
        transitionMap = convertTransitions();
        bundleMap = convertBundles();
        groupLocalities();
        convertConnections();
    }

    private void convertTitle() {
        petriNet.setTitle(policyNet.getTitle());
    }

    private Map<VisualPlace, VisualPlace> convertPlaces() {
        Map<VisualPlace, VisualPlace> result = new HashMap<>();
        for (VisualPlace place : policyNet.getVisualPlaces()) {
            String name = policyNet.getMathModel().getNodeReference(place.getReferencedComponent());
            VisualPlace newPlace = petriNet.createPlace(name, null);
            newPlace.copyPosition(place);
            newPlace.copyStyle(place);
            result.put(place, newPlace);
        }
        return result;
    }

    private Map<VisualBundledTransition, VisualTransition> convertTransitions() {
        Map<VisualBundledTransition, VisualTransition> result = new HashMap<>();
        for (VisualBundledTransition transition : policyNet.getVisualBundledTransitions()) {
            Collection<Bundle> bundles = policyNet.getMathModel().getBundlesOfTransition(transition.getReferencedComponent());
            if (bundles.isEmpty()) {
                String name = policyNet.getMathModel().getNodeReference(transition.getReferencedComponent());
                VisualTransition newTransition = petriNet.createTransition(name, null);
                newTransition.copyPosition(transition);
                newTransition.copyStyle(transition);
                result.put(transition, newTransition);
            }
        }
        return result;
    }

    private Map<VisualBundle, VisualTransition> convertBundles() {
        Map<VisualBundle, VisualTransition> result = new HashMap<>();
        for (VisualBundle bundle : policyNet.getVisualBundles()) {
            if (!bundle.getReferencedBundle().isEmpty()) {
                double x = 0;
                double y = 0;
                int count = 0;
                for (VisualBundledTransition transition : policyNet.getVisualBundledTransitions()) {
                    if (bundle.getReferencedBundle().contains(transition.getReferencedComponent())) {
                        x += transition.getX();
                        y += transition.getY();
                        count++;
                    }
                }
                if (count > 0) {
                    String bundleName = policyNet.getMathModel().getNodeReference(bundle.getReferencedBundle());
                    VisualTransition newTransition = petriNet.createTransition(bundleName, null);
                    newTransition.setFillColor(bundle.getColor());
                    newTransition.setPosition(new Point2D.Double(x / count, y / count));
                    result.put(bundle, newTransition);
                }
            }
        }
        return result;
    }

    private void groupLocalities() {
        for (VisualLocality locality : policyNet.getVisualLocalities()) {
            HashSet<VisualNode> nodes = new HashSet<>();
            for (Node node: locality.getChildren()) {
                if (node instanceof VisualBundledTransition) {
                    VisualTransition t = transitionMap.get(node);
                    if (t != null) {
                        nodes.add(t);
                    }
                    for (VisualBundle b: policyNet.getBundlesOfTransition((VisualBundledTransition) node)) {
                        VisualTransition bt = bundleMap.get(b);
                        if (bt != null) {
                            nodes.add(bt);
                        }
                    }
                } else if (node instanceof VisualPlace) {
                    VisualPlace p = placeMap.get(node);
                    if (p != null) {
                        nodes.add(p);
                    }
                }
            }
            petriNet.select(nodes);
            petriNet.groupSelection();
            petriNet.selectNone();
        }
    }

    private void convertConnections() {
        for (VisualConnection srcConnection : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualConnection.class)) {
            try {
                convertConnection(srcConnection);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void convertConnection(VisualConnection srcConnection) throws InvalidConnectionException {
        Collection<VisualNode> dstFromNodes = getDstNodes(srcConnection.getFirst());
        Collection<VisualNode> dstToNodes = getDstNodes(srcConnection.getSecond());
        boolean isReadArc = srcConnection instanceof VisualReadArc;
        Collection<VisualConnection> dstConnections = createDstConnections(dstFromNodes, dstToNodes, isReadArc);
        for (VisualConnection dstConnection : dstConnections) {
            dstConnection.copyStyle(srcConnection);
            dstConnection.copyShape(srcConnection);
        }
    }

    private Collection<VisualConnection> createDstConnections(Collection<VisualNode> dstFromNodes,
            Collection<VisualNode> dstToNodes, boolean isReadArc) throws InvalidConnectionException {

        Collection<VisualConnection> result = new HashSet<>();
        for (VisualNode dstFromNode : dstFromNodes) {
            for (VisualNode dstToNode : dstToNodes) {
                VisualConnection dstConnection = isReadArc
                        ? petriNet.connectUndirected(dstFromNode, dstToNode)
                        : petriNet.connect(dstFromNode, dstToNode);

                result.add(dstConnection);
            }
        }
        return result;
    }

    private Collection<VisualNode> getDstNodes(VisualNode srcNode) {
        Collection<VisualNode> result = new HashSet<>();
        if (srcNode instanceof VisualPlace) {
            VisualPlace dstPlace = placeMap.get(srcNode);
            if (dstPlace != null) {
                result.add(dstPlace);
            }
        }

        if (srcNode instanceof VisualBundledTransition) {
            VisualBundledTransition stcTransition = (VisualBundledTransition) srcNode;
            Collection<VisualBundle> srcBundles = policyNet.getBundlesOfTransition(stcTransition);
            for (VisualBundle srcBundle :srcBundles) {
                VisualTransition dstTransition = bundleMap.get(srcBundle);
                if (dstTransition != null) {
                    result.add(dstTransition);
                }
            }
            VisualTransition dstTransition = transitionMap.get(srcNode);
            if (dstTransition != null) {
                result.add(dstTransition);
            }
        }

        if (srcNode instanceof VisualReplica) {
            VisualComponent srcMasterNode = ((VisualReplica) srcNode).getMaster();
            result.addAll(getDstNodes(srcMasterNode));
        }
        return result;
    }

    public VisualPolicy getPolicyNet() {
        return policyNet;
    }

    public VisualPetri getPetriNet() {
        return petriNet;
    }

    public VisualPlace getRelatedPlace(VisualPlace node) {
        return placeMap.get(node);
    }

    public Collection<VisualTransition> getRelatedTransitions(VisualBundledTransition node) {
        HashSet<VisualTransition> result = new HashSet<>();
        VisualTransition t = transitionMap.get(node);
        if (t != null) {
            result.add(t);
        } else {
            for (VisualBundle bundle: policyNet.getBundlesOfTransition(node)) {
                VisualTransition bt = bundleMap.get(bundle);
                if (bt != null) {
                    result.add(bt);
                }
            }
        }
        return result;
    }

    public boolean isRelated(Node highLevelNode, Node node) {
        boolean result = false;
        if (highLevelNode instanceof VisualBundledTransition) {
            for (VisualTransition relatedTransition: getRelatedTransitions((VisualBundledTransition) highLevelNode)) {
                if ((node == relatedTransition) || (node == relatedTransition.getReferencedComponent())) {
                    result = true;
                    break;
                }
            }
        } else if (highLevelNode instanceof VisualPlace) {
            VisualPlace relatedPlace = getRelatedPlace((VisualPlace) highLevelNode);
            if (relatedPlace != null) {
                result = (node == relatedPlace) || (node == relatedPlace.getReferencedComponent());
            }
        }
        return result;
    }

}
