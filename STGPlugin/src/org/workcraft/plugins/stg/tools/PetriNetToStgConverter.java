package org.workcraft.plugins.stg.tools;

import java.util.HashMap;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriNetToStgConverter implements Tool {

	@Override
	public String getDisplayName() {
		return "Signal Transition Graph";
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNet;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualPetriNet pn = (VisualPetriNet)we.getModelEntry().getVisualModel();
		final VisualSTG stg = convertNet(pn);
		final Framework framework = Framework.getInstance();
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String name = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new STGModelDescriptor(), stg);
		workspace.add(directory, name, me, false, true);
	}

	private VisualSTG convertNet(VisualPetriNet pn) {
		VisualSTG stg = new VisualSTG(new STG());
		HashMap<VisualPlace, VisualPlace> place2place = convertPlaces(pn, stg);
		HashMap<VisualTransition, VisualDummyTransition> transition2transition = convertTransitions(pn, stg);

		HashMap<VisualComponent, VisualComponent> component2component = new HashMap<>();
		component2component.putAll(place2place);
		component2component.putAll(transition2transition);
		convertConnections(pn, stg, component2component);

		return stg;
	}

	private HashMap<VisualPlace, VisualPlace> convertPlaces(VisualPetriNet pn, VisualSTG stg) {
		HashMap<VisualPlace, VisualPlace> result = new HashMap<>();
		for(VisualPlace place : Hierarchy.getDescendantsOfType(pn.getRoot(), VisualPlace.class)) {
			VisualPlace newPlace = stg.createPlace(pn.getPetriNet().getNodeReference(place.getReferencedPlace()), null);
			newPlace.copyProperties(place);
			result.put(place, newPlace);
		}
		return result;
	}

	private HashMap<VisualTransition, VisualDummyTransition> convertTransitions(VisualPetriNet pn, VisualSTG stg) {
		HashMap<VisualTransition, VisualDummyTransition> result = new HashMap<>();
		for(VisualTransition transition : Hierarchy.getDescendantsOfType(pn.getRoot(), VisualTransition.class)) {
			VisualDummyTransition newTransition = stg.createDummyTransition(pn.getPetriNet().getNodeReference(transition.getReferencedTransition()), null);
			newTransition.copyProperties(transition);
			result.put(transition, newTransition);
		}
		return result;
	}

	private HashMap<VisualConnection, VisualConnection> convertConnections(VisualPetriNet pn, VisualSTG stg,
			HashMap<VisualComponent, VisualComponent> component2component) {
		HashMap<VisualConnection, VisualConnection> result = new HashMap<>();
		for(VisualConnection connection : Hierarchy.getDescendantsOfType(pn.getRoot(), VisualConnection.class)) {
			VisualComponent first = connection.getFirst();
			VisualComponent second = connection.getSecond();
			VisualComponent newFirst = component2component.get(first);
			VisualComponent newSecond= component2component.get(second);
			if ((newFirst != null) && (newSecond != null)) {
				try {
					VisualConnection newConnection = stg.connect(newFirst, newSecond);
					newConnection.copyProperties(connection);
					newConnection.copyGeometry(connection);
					result.put(connection, newConnection);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

}
