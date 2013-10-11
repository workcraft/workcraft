package org.workcraft.plugins.policy.tools;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualLocality;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.util.Hierarchy;

public class PetriNetGenerator {
	private final VisualPolicyNet policyNet;
	private final VisualPetriNet petriNet;

	private final Map<VisualPlace, VisualPlace> placeMap;
	private final Map<VisualBundledTransition, VisualTransition> transitionMap;
	private final Map<VisualLocality, VisualGroup> localityMap;
	private final Map<Bundle, VisualTransition> bundleMap;

	public PetriNetGenerator(VisualPolicyNet policyNet) {
		this.policyNet = policyNet;
		try {
			this.petriNet = new VisualPetriNet(new PetriNet());
			placeMap = convertPlaces();
			transitionMap = convertTransitions();
			bundleMap = convertBundles();
			localityMap = convertLocalities();
			connectTransitions();
			connectBundles();
		} catch (VisualModelInstantiationException e) {
			throw new RuntimeException(e);
		} catch ( InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<VisualPlace, VisualPlace> convertPlaces() {
		Map<VisualPlace, VisualPlace> result = new HashMap<VisualPlace, VisualPlace>();
		for(VisualPlace place : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualPlace.class)) {
			VisualPlace newPlace = petriNet.createPlace(policyNet.getPolicyNet().getNodeReference(place.getReferencedPlace()));
			newPlace.setPosition(place.getPosition());
			newPlace.getReferencedPlace().setCapacity(place.getReferencedPlace().getCapacity());
			newPlace.getReferencedPlace().setTokens(place.getReferencedPlace().getTokens());
			newPlace.setForegroundColor(place.getForegroundColor());
			newPlace.setFillColor(place.getFillColor());
			newPlace.setTokenColor(place.getTokenColor());
			newPlace.setLabel(place.getLabel());
			newPlace.setLabelColor(place.getLabelColor());
			newPlace.setLabelPositioning(place.getLabelPositioning());
			result.put(place, newPlace);
		}
		return result;
	}

	private Map<VisualBundledTransition, VisualTransition> convertTransitions() {
		Map<VisualBundledTransition, VisualTransition> result = new HashMap<VisualBundledTransition, VisualTransition>();
		for(VisualBundledTransition transition : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualBundledTransition.class)) {
			Collection<Bundle> bundles = policyNet.getPolicyNet().getBundlesOfTransition(transition.getReferencedTransition());
			if (bundles.size() == 0) {
				VisualTransition newTransition = petriNet.createTransition(policyNet.getPolicyNet().getNodeReference(transition.getReferencedTransition()));
				newTransition.setPosition(transition.getPosition());
				newTransition.setForegroundColor(transition.getForegroundColor());
				newTransition.setFillColor(transition.getFillColor());
				newTransition.setLabel(transition.getLabel());
				newTransition.setLabelColor(transition.getLabelColor());
				newTransition.setLabelPositioning(transition.getLabelPositioning());
				result.put(transition, newTransition);
			}
		}
		return result;
	}

	private Map<Bundle, VisualTransition> convertBundles() {
		Map<Bundle, VisualTransition> result = new HashMap<Bundle, VisualTransition>();
		for(Bundle bundle : Hierarchy.getDescendantsOfType(policyNet.getPolicyNet().getRoot(), Bundle.class)) {
			if (!bundle.isEmpty()) {
				double x = 0;
				double y = 0;
				int count = 0;
				for(VisualBundledTransition transition : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualBundledTransition.class)) {
					if (bundle.contains(transition.getReferencedTransition())) {
						x += transition.getX();
						y += transition.getY();
						count++;
					}
				}
				if (count > 0) {
					VisualTransition newTransition = petriNet.createTransition(policyNet.getPolicyNet().getNodeReference(bundle));
					newTransition.setFillColor(bundle.getColor());
					newTransition.setPosition(new Point2D.Double(x / count, y / count));
					result.put(bundle, newTransition);
				}
			}
		}
		return result;
	}

	private Map<VisualLocality, VisualGroup> convertLocalities() {
		Map<VisualLocality, VisualGroup> result = new HashMap<VisualLocality, VisualGroup>();
		for(VisualLocality locality : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualLocality.class)) {
			HashSet<Node> nodes = new HashSet<Node>();
			for (Node node: locality.getChildren()) {
				if (node instanceof VisualBundledTransition) {
					VisualTransition t = transitionMap.get(node);
					if (t != null) {
						nodes.add(t);
					}
					for (Bundle b: policyNet.getBundlesOfTransition((VisualBundledTransition)node)) {
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
		return result;
	}

	private void connectTransitions() throws InvalidConnectionException {
		for(VisualBundledTransition transition : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualBundledTransition.class)) {
			VisualTransition newTransition = transitionMap.get(transition);
			if (newTransition != null) {
				for (Node node: policyNet.getPreset(transition)) {
					if (node instanceof VisualPlace) {
						VisualPlace newPlace = placeMap.get(node);
						if (newPlace != null) {
							petriNet.connect(newPlace, newTransition);
						}
					}
				}
				for (Node node: policyNet.getPostset(transition)) {
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
		for(Bundle bundle : policyNet.getPolicyNet().getBundles()) {
			VisualTransition newTransition = bundleMap.get(bundle);
			if (newTransition != null) {
				for(VisualBundledTransition t: policyNet.getTransitionsOfBundle(bundle)) {
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

	public VisualPetriNet getPetriNet() {
		return petriNet;
	}

	public VisualPlace getRelatedPlace(VisualPlace node) {
		return placeMap.get(node);
	}

	public Collection<VisualTransition> getRelatedTransitions(VisualBundledTransition node) {
		HashSet<VisualTransition> result = new HashSet<VisualTransition>();
		VisualTransition t = transitionMap.get(node);
		if (t != null) {
			result.add(t);
		} else {
			for (Bundle bundle: policyNet.getBundlesOfTransition(node)) {
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
			for (VisualTransition t: getRelatedTransitions((VisualBundledTransition)highLevelNode)) {
				if (t == node) {
					result = true;
					break;
				}
			}
		} else if (highLevelNode instanceof VisualPlace) {
			result = (node == getRelatedPlace((VisualPlace)highLevelNode));
		}
		return result;
	}

}
