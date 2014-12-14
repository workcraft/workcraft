package org.workcraft.plugins.stg.tools;

import java.util.HashMap;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetModelDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToPetriNetConverter implements Tool {

	@Override
	public String getDisplayName() {
		return "Petri Net";
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		final VisualPetriNet pn = convertNet(stg);
		final Framework framework = Framework.getInstance();
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String name = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new PetriNetModelDescriptor(), pn);
		workspace.add(directory, name, me, false, true);
	}

	private VisualPetriNet convertNet(VisualSTG stg) {
		VisualPetriNet pn = new VisualPetriNet(new PetriNet());
		HashMap<VisualPlace, VisualPlace> place2place = convertPlaces(stg, pn);
		HashMap<VisualNamedTransition, VisualTransition> transition2transition = convertTransitions(stg, pn);

		HashMap<VisualComponent, VisualComponent> component2component = new HashMap<>();
		component2component.putAll(place2place);
		component2component.putAll(transition2transition);
		convertConnections(stg, pn, component2component);

		return pn;
	}

	private HashMap<VisualPlace, VisualPlace> convertPlaces(VisualSTG stg, VisualPetriNet pn) {
		HashMap<VisualPlace, VisualPlace> result = new HashMap<>();
		for(VisualPlace place : Hierarchy.getDescendantsOfType(stg.getRoot(), VisualPlace.class)) {
			VisualPlace newPlace = pn.createPlace(stg.getMathModel().getNodeReference(place.getReferencedPlace()));
			newPlace.copyProperties(place);
			result.put(place, newPlace);
		}
		return result;
	}

	private HashMap<VisualNamedTransition, VisualTransition> convertTransitions(VisualSTG stg, VisualPetriNet pn) {
		HashMap<VisualNamedTransition, VisualTransition> result = new HashMap<>();
		for(VisualNamedTransition transition : Hierarchy.getDescendantsOfType(stg.getRoot(), VisualNamedTransition.class)) {
			VisualTransition newTransition = pn.createTransition(stg.getMathModel().getNodeReference(transition.getReferencedTransition()));
			newTransition.copyProperties(transition);
			result.put(transition, newTransition);
		}
		return result;
	}

	private HashMap<VisualConnection, VisualConnection> convertConnections(VisualSTG stg, VisualPetriNet pn,
			HashMap<VisualComponent, VisualComponent> component2component) {
		HashMap<VisualConnection, VisualConnection> result = new HashMap<>();
		for(VisualConnection connection : Hierarchy.getDescendantsOfType(stg.getRoot(), VisualConnection.class)) {
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
