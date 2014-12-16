package org.workcraft.plugins.stg.tools;

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Hierarchy;

public class PetriNetToStgConverter {
	final private VisualPetriNet pn;
	final private VisualSTG stg;
	final private HashMap<String, Container> containers;
	final HashMap<VisualComponent, VisualComponent> pn2stg;

	public PetriNetToStgConverter(VisualPetriNet pn) {
		this.pn = pn;
		this.stg = new VisualSTG(new STG());
		this.containers = NamespaceHelper.copyPageStructure(stg, stg.getRoot(), pn, pn.getRoot(), null);
		this.pn2stg = new HashMap<>();
		convertPlaces();
		convertTransitions();
		convertConnections();
		convertGroups();
	}

	private void convertPlaces() {
		for(VisualPlace pnPlace : Hierarchy.getDescendantsOfType(pn.getRoot(), VisualPlace.class)) {
			String ref = pn.getNodeMathReference(pnPlace);
			if (ref != null) {
				String path = NamespaceHelper.getParentReference(ref);
				String name = NamespaceHelper.getNameFromReference(ref);
				Container container = containers.get(path);
				VisualPlace stgPlace = stg.createPlace(name, container);
				stgPlace.copyProperties(pnPlace);
				pn2stg.put(pnPlace, stgPlace);
			}
		}
	}

	private void convertTransitions() {
		for(VisualTransition pnTransition : Hierarchy.getDescendantsOfType(pn.getRoot(), VisualTransition.class)) {
			String ref = pn.getNodeMathReference(pnTransition);
			if (ref != null) {
				String path = NamespaceHelper.getParentReference(ref);
				String name = NamespaceHelper.getNameFromReference(ref);
				Container container = containers.get(path);
				VisualDummyTransition stgTransition = stg.createDummyTransition(name, container);
				stgTransition.copyStyle(pnTransition);
				pn2stg.put(pnTransition, stgTransition);
			}
		}
	}

	private void convertConnections() {
		for(VisualConnection connection : Hierarchy.getDescendantsOfType(pn.getRoot(), VisualConnection.class)) {
			VisualComponent first = connection.getFirst();
			VisualComponent second = connection.getSecond();
			VisualComponent newFirst = pn2stg.get(first);
			VisualComponent newSecond= pn2stg.get(second);
			if ((newFirst != null) && (newSecond != null)) {
				try {
					VisualConnection newConnection = stg.connect(newFirst, newSecond);
					newConnection.copyStyle(connection);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void convertGroups() {
		for(VisualGroup pnGroup: Hierarchy.getDescendantsOfType(pn.getRoot(), VisualGroup.class)) {
			HashSet<Node> stgSelection = new HashSet<>();
			for (Node pnNode: pnGroup.getChildren()) {
				Node stgNode = pn2stg.get(pnNode);
				if (stgNode != null) {
					stgSelection.add(stgNode);
				}
			}
			if ( !stgSelection.isEmpty() ) {
				stg.addToSelection(stgSelection);
				VisualGroup stgGroup = stg.groupSelection();
				stgGroup.copyStyle(pnGroup);
			}
		}
		stg.selectNone();
	}

	public VisualPetriNet getPetriNet() {
		return pn;
	}

	public VisualSTG getStg() {
		return stg;
	}

}
