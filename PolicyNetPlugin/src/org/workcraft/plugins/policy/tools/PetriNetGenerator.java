package org.workcraft.plugins.policy.tools;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.util.Hierarchy;

public class PetriNetGenerator {
	private final VisualPolicyNet policyNet;
	private final VisualPetriNet petriNet;

	private final Map<VisualPlace, VisualPlace> placeMap;
	private final Map<VisualBundledTransition, VisualTransition> transitionMap;
	private final Map<Bundle, VisualTransition> bundleMap;

	public PetriNetGenerator(VisualPolicyNet policyNet) {
		this.policyNet = policyNet;
		try {
			this.petriNet = new VisualPetriNet(new PetriNet());
			placeMap = generatePlaces();
			transitionMap = generateTransitions();
			bundleMap = generateBundles();
			connectTransitions();
			connectBundles();
		} catch (VisualModelInstantiationException e) {
			throw new RuntimeException(e);
		} catch ( InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<VisualPlace, VisualPlace> generatePlaces() {
		Map<VisualPlace, VisualPlace> result = new HashMap<VisualPlace, VisualPlace>();
		for(VisualPlace place : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualPlace.class)) {
			VisualPlace newPlace = petriNet.createPlace(policyNet.getPolicyNet().getNodeReference(place.getReferencedPlace()));
			newPlace.setPosition(place.getPosition());
			newPlace.setCapacity(place.getCapacity());
			newPlace.setTokens(place.getTokens());
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

	private Map<VisualBundledTransition, VisualTransition> generateTransitions() {
		Map<VisualBundledTransition, VisualTransition> result = new HashMap<VisualBundledTransition, VisualTransition>();
		for(VisualBundledTransition transition : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualBundledTransition.class)) {
			Collection<Bundle> bundles = policyNet.getPolicyNet().getTransitionBundles(transition.getReferencedTransition());
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

	private Map<Bundle, VisualTransition> generateBundles() {
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
		for(Bundle bundle : Hierarchy.getDescendantsOfType(policyNet.getPetriNet().getRoot(), Bundle.class)) {
			VisualTransition newTransition = bundleMap.get(bundle);
			if (newTransition != null) {
				for(VisualBundledTransition transition : Hierarchy.getDescendantsOfType(policyNet.getRoot(), VisualBundledTransition.class)) {
					if (bundle.contains(transition.getReferencedTransition())) {
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
		}
	}

	public VisualPetriNet getPetriNet() {
		return petriNet;
	}

}
