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
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Hierarchy;

public class StgToPetriNetConverter  {
	final private VisualPetriNet pn;
	final private VisualSTG stg;
	final private HashMap<String, Container> containers;
	final HashMap<VisualComponent, VisualComponent> stg2pn;

	public StgToPetriNetConverter(VisualSTG stg) {
		this.stg = stg;
		this.pn = new VisualPetriNet(new PetriNet());
		this.containers = NamespaceHelper.copyPageStructure(pn, pn.getRoot(), stg, stg.getRoot(), null);
		this.stg2pn = new HashMap<>();

		convertPlaces();
		convertTransitions();
		convertConnections();
		convertGroups();
	}

	private void convertPlaces() {
		for(VisualPlace stgPlace : Hierarchy.getDescendantsOfType(stg.getRoot(), VisualPlace.class)) {
			String ref = stg.getNodeMathReference(stgPlace);
			if (ref != null) {
				String path = NamespaceHelper.getParentReference(ref);
				String name = NamespaceHelper.getNameFromReference(ref);
				Container container = containers.get(path);
				VisualPlace pnPlace = pn.createPlace(name, container);
				pnPlace.copyProperties(stgPlace);
				stg2pn.put(stgPlace, pnPlace);
			}
		}
	}

	private void convertTransitions() {
		for(VisualNamedTransition stgTransition : Hierarchy.getDescendantsOfType(stg.getRoot(), VisualNamedTransition.class)) {
			String ref = stg.getNodeMathReference(stgTransition);
			if (ref != null) {
				String path = NamespaceHelper.getParentReference(ref);
				String name = NamespaceHelper.getNameFromReference(ref);
				Container container = containers.get(path);
				VisualTransition pnTransition = pn.createTransition(name, container);
				pnTransition.copyStyle(stgTransition);
				stg2pn.put(stgTransition, pnTransition);
			}
		}
	}

	private void convertConnections() {
		for(VisualConnection stgConnection : Hierarchy.getDescendantsOfType(stg.getRoot(), VisualConnection.class)) {
			VisualComponent stgFirst = stgConnection.getFirst();
			VisualComponent stgSecond = stgConnection.getSecond();
			VisualComponent pnFirst = stg2pn.get(stgFirst);
			VisualComponent pnSecond= stg2pn.get(stgSecond);
			if ((pnFirst != null) && (pnSecond != null)) {
				try {
					VisualConnection pnConnection = stg.connect(pnFirst, pnSecond);
					pnConnection.copyStyle(stgConnection);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void convertGroups() {
		for(VisualGroup stgGroup: Hierarchy.getDescendantsOfType(stg.getRoot(), VisualGroup.class)) {
			HashSet<Node> pnSelection = new HashSet<>();
			for (Node stgNode: stgGroup.getChildren()) {
				Node pnNode = stg2pn.get(stgNode);
				if (pnNode != null) {
					pnSelection.add(pnNode);
				}
			}
			if ( !pnSelection.isEmpty() ) {
				pn.addToSelection(pnSelection);
				VisualGroup pnGroup = pn.groupSelection();
				pnGroup.copyStyle(stgGroup);
			}
		}
		pn.selectNone();
	}

	public VisualPetriNet getPetriNet() {
		return pn;
	}

	public VisualSTG getStg() {
		return stg;
	}

}
