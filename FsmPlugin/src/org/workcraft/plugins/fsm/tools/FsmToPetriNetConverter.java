package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Hierarchy;

public class FsmToPetriNetConverter {
	private final VisualFsm srcModel;
	private final VisualPetriNet dstModel;

	private final Map<VisualState, VisualPlace> stateToPlaceMap;
	private final Map<VisualEvent, VisualTransition> eventToTransitionMap;
	private final Map<String, String> refToSymbolMap;

	public FsmToPetriNetConverter(VisualFsm srcModel, VisualPetriNet dstModel) {
		this.srcModel = srcModel;
		this.dstModel = dstModel;
		stateToPlaceMap = convertStates();
		eventToTransitionMap = convertEvents();
		refToSymbolMap = cacheLabels();
		try {
			connectEvents();
		} catch ( InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> cacheLabels() {
		Map<String, String> result = new HashMap<String, String>();
		for (Entry<VisualEvent, VisualTransition> entry: eventToTransitionMap.entrySet()) {
			VisualEvent event = entry.getKey();
			VisualTransition transition = entry.getValue();
			String ref = dstModel.getPetriNet().getNodeReference(transition.getReferencedTransition());
			String symbol = event.getReferencedEvent().getSymbol();
			result.put(ref, symbol);
		}
		return result;
	}

	private Map<VisualState, VisualPlace> convertStates() {
		Map<VisualState, VisualPlace> result = new HashMap<VisualState, VisualPlace>();
		for(VisualState state: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualState.class)) {
			String name = srcModel.getMathModel().getNodeReference(state.getReferencedState());
			VisualPlace place = dstModel.createPlace(name, null);
			place.copyStyle(state);
			place.getReferencedPlace().setTokens(state.getReferencedState().isInitial() ? 1 : 0);
			place.setTokenColor(state.getForegroundColor());
			result.put(state, place);
		}
		return result;
	}

	private Map<VisualEvent, VisualTransition> convertEvents() {
		Map<VisualEvent, VisualTransition> result = new HashMap<VisualEvent, VisualTransition>();
		for(VisualEvent event : Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualEvent.class)) {
			String name = srcModel.getMathModel().getNodeReference(event.getReferencedConnection());
			VisualTransition transition = dstModel.createTransition(name, null);
			transition.setPosition(event.getCenter());
			transition.setForegroundColor(event.getColor());
			transition.setLabel(event.getReferencedEvent().getSymbol());
			transition.setLabelColor(event.getSymbolColor());
			result.put(event, transition);
		}
		return result;
	}

	private void connectEvents() throws InvalidConnectionException {
		for(VisualEvent event: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualEvent.class)) {
			VisualTransition transition = eventToTransitionMap.get(event);
			if (transition != null) {
				Node first = event.getFirst();
				if (first instanceof VisualState) {
					VisualPlace inPlace = stateToPlaceMap.get(first);
					if (inPlace != null) {
						dstModel.connect(inPlace, transition);
					}
				}
				Node second = event.getSecond();
				if (second instanceof VisualState) {
					VisualPlace outPlace = stateToPlaceMap.get(second);
					if (outPlace != null) {
						dstModel.connect(transition, outPlace);
					}
				}
			}
		}
	}

	public VisualFsm getSrcModel() {
		return srcModel;
	}

	public VisualPetriNet getDstModel() {
		return dstModel;
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
