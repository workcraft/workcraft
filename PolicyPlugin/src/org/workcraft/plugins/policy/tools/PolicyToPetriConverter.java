package org.workcraft.plugins.policy.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.policy.*;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PolicyToPetriConverter {
    private final VisualPolicyNet policyNet;
    private final VisualPetriNet petriNet;

    private final Map<VisualPlace, VisualPlace> placeMap;
    private final Map<VisualBundledTransition, VisualTransition> transitionMap;
    private final Map<VisualBundle, VisualTransition> bundleMap;

    public PolicyToPetriConverter(VisualPolicyNet policyNet) {
        this.policyNet = policyNet;
        this.petriNet = new VisualPetriNet(new PetriNet());
        placeMap = convertPlaces();
        transitionMap = convertTransitions();
        bundleMap = convertBundles();
        groupLocalities();
        try {
            connectTransitions();
            connectBundles();
        } catch (InvalidConnectionException e) {
            //throw new RuntimeException(e);
        }
    }

    private Map<VisualPlace, VisualPlace> convertPlaces() {
        Map<VisualPlace, VisualPlace> result = new HashMap<>();
        for (VisualPlace place : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualPlace.class)) {
            String name = policyNet.getMathModel().getNodeReference(place.getReferencedPlace());
            VisualPlace newPlace = petriNet.createPlace(name, null);
            newPlace.copyPosition(place);
            newPlace.copyStyle(place);
            result.put(place, newPlace);
        }
        return result;
    }

    private Map<VisualBundledTransition, VisualTransition> convertTransitions() {
        Map<VisualBundledTransition, VisualTransition> result = new HashMap<>();
        for (VisualBundledTransition transition : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualBundledTransition.class)) {
            Collection<Bundle> bundles = policyNet.getMathModel().getBundlesOfTransition(transition.getReferencedTransition());
            if (bundles.size() == 0) {
                String name = policyNet.getMathModel().getNodeReference(transition.getReferencedTransition());
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
                    if (bundle.getReferencedBundle().contains(transition.getReferencedTransition())) {
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
        for (VisualLocality locality : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualLocality.class)) {
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

    private void connectTransitions() throws InvalidConnectionException {
        for (VisualBundledTransition transition : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualBundledTransition.class)) {
            VisualTransition newTransition = transitionMap.get(transition);
            if (newTransition != null) {
                for (VisualNode node: policyNet.getPreset(transition)) {
                    if (node instanceof VisualPlace) {
                        VisualPlace newPlace = placeMap.get(node);
                        if (newPlace != null) {
                            petriNet.connect(newPlace, newTransition);
                        }
                    }
                }
                for (VisualNode node: policyNet.getPostset(transition)) {
                    if (node instanceof VisualPlace) {
                        VisualPlace newPlace = placeMap.get(node);
                        if (newPlace != null) {
                            petriNet.connect(newTransition, newPlace);
                        }
                    }
                }
            }
        }
    }

    private void connectBundles() throws InvalidConnectionException {
        for (VisualBundle bundle : policyNet.getVisualBundles()) {
            VisualTransition newTransition = bundleMap.get(bundle);
            if (newTransition != null) {
                for (VisualBundledTransition t: policyNet.getTransitionsOfBundle(bundle)) {
                    for (Node node: policyNet.getPreset(t)) {
                        if (node instanceof VisualPlace) {
                            VisualPlace newPlace = placeMap.get(node);
                            if (newPlace != null) {
                                petriNet.connect(newPlace, newTransition);
                            }
                        }
                    }
                    for (Node node: policyNet.getPostset(t)) {
                        if (node instanceof VisualPlace) {
                            VisualPlace newPlace = placeMap.get(node);
                            if (newPlace != null) {
                                petriNet.connect(newTransition, newPlace);
                            }
                        }
                    }
                }
            }
        }
    }

    public VisualPolicyNet getPolicyNet() {
        return policyNet;
    }

    public VisualPetriNet getPetriNet() {
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
