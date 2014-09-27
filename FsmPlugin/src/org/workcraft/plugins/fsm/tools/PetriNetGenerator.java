package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Hierarchy;

public class PetriNetGenerator {
	private final VisualFsm fsm;
	private final VisualPetriNet petriNet;

	private final Map<VisualState, VisualPlace> stateToPlaceMap;
	private final Map<VisualEvent, VisualTransition> eventToTransitionMap;
	private final Map<String, String> refToSymbolMap;

	public PetriNetGenerator(VisualFsm fsm) {
		this.fsm = fsm;
		try {
			this.petriNet = new VisualPetriNet(new PetriNet());
			stateToPlaceMap = convertStates();
			eventToTransitionMap = convertEvents();
			refToSymbolMap = cacheLabels();
			connectEvents();
		} catch (VisualModelInstantiationException e) {
			throw new RuntimeException(e);
		} catch ( InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> cacheLabels() {
		Map<String, String> result = new HashMap<String, String>();
		for (Entry<VisualEvent, VisualTransition> entry: eventToTransitionMap.entrySet()) {
			VisualEvent event = entry.getKey();
			VisualTransition transition = entry.getValue();
			String ref = petriNet.getPetriNet().getNodeReference(transition.getReferencedTransition());
			String symbol = event.getReferencedEvent().getSymbol();
			result.put(ref, symbol);
		}
		return result;
	}

	private Map<VisualState, VisualPlace> convertStates() {
		Map<VisualState, VisualPlace> result = new HashMap<VisualState, VisualPlace>();
		for(VisualState state: Hierarchy.getDescendantsOfType(fsm.getRoot(), VisualState.class)) {
			VisualPlace place = petriNet.createPlace(fsm.getMathModel().getNodeReference(state.getReferencedState()));
			place.setPosition(state.getPosition());
			place.getReferencedPlace().setTokens(state.getReferencedState().isInitial() ? 1 : 0);
			place.setForegroundColor(state.getForegroundColor());
			place.setFillColor(state.getFillColor());
			place.setTokenColor(state.getForegroundColor());
			place.setLabel(state.getLabel());
			place.setLabelColor(state.getLabelColor());
			place.setLabelPositioning(state.getLabelPositioning());
			result.put(state, place);
		}
		return result;
	}

	private Map<VisualEvent, VisualTransition> convertEvents() {
		Map<VisualEvent, VisualTransition> result = new HashMap<VisualEvent, VisualTransition>();
		for(VisualEvent event : Hierarchy.getDescendantsOfType(fsm.getRoot(), VisualEvent.class)) {
			VisualTransition transition = petriNet.createTransition(fsm.getMathModel().getNodeReference(event.getReferencedConnection()));
			transition.setPosition(event.getCenter());
			transition.setForegroundColor(event.getColor());
			transition.setLabel(event.getReferencedEvent().getSymbol());
			transition.setLabelColor(event.getSymbolColor());
			result.put(event, transition);
		}
		return result;
	}

	private void connectEvents() throws InvalidConnectionException {
		for(VisualEvent event: Hierarchy.getDescendantsOfType(fsm.getRoot(), VisualEvent.class)) {
			VisualTransition transition = eventToTransitionMap.get(event);
			if (transition != null) {
				Node first = event.getFirst();
				if (first instanceof VisualState) {
					VisualPlace inPlace = stateToPlaceMap.get(first);
					if (inPlace != null) {
						petriNet.connect(inPlace, transition);
					}
				}
				Node second = event.getSecond();
				if (second instanceof VisualState) {
					VisualPlace outPlace = stateToPlaceMap.get(second);
					if (outPlace != null) {
						petriNet.connect(transition, outPlace);
					}
				}
			}
		}
	}

	public VisualFsm getFsm() {
		return fsm;
	}

	public VisualPetriNet getPetriNet() {
		return petriNet;
	}

	public VisualPlace getRelatedPlace(VisualState state) {
		return stateToPlaceMap.get(state);
	}

	public VisualTransition getRelatedTransition(VisualEvent event) {
		return eventToTransitionMap.get(event);
	}

	public boolean isRelated(Node highLevelNode, Node node) {
		boolean result = false;
		if (highLevelNode instanceof VisualEvent) {
			result = (node == getRelatedTransition((VisualEvent)highLevelNode));
		} else if (highLevelNode instanceof VisualState) {
			result = (node == getRelatedPlace((VisualState)highLevelNode));
		}
		return result;
	}

	public String getSymbol(String ref) {
		return refToSymbolMap.get(ref);
	}

}
