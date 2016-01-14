package org.workcraft.plugins.petri.tools;

import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.workspace.WorkspaceEntry;

public class ReplicateReadArcPlaceTool extends TransformationTool implements NodeTransformer {

	@Override
	public String getDisplayName() {
		return "Create proxies for read-arc places (selected or all)";
	}

	@Override
	public boolean isApplicableTo(Node node) {
		return (node instanceof VisualReadArc);
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNetModel;
	}

	@Override
	public Position getPosition() {
		return Position.MIDDLE;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualModel visualModel = we.getModelEntry().getVisualModel();
		HashSet<VisualReadArc> readArcs = PetriNetUtils.getVisualReadArcs(visualModel);
		if ( !visualModel.getSelection().isEmpty() ) {
			readArcs.retainAll(visualModel.getSelection());
		}
		HashSet<VisualPlace> places = PetriNetUtils.getVisualPlaces(visualModel);
		if ( !visualModel.getSelection().isEmpty() ) {
			places.retainAll(visualModel.getSelection());
		}
		for (VisualPlace place: places) {
			for (Connection connection: visualModel.getConnections(place)) {
				if (connection instanceof VisualReadArc) {
					readArcs.add((VisualReadArc)connection);
				}
			}
		}

		if ( !readArcs.isEmpty() ) {
			we.saveMemento();
			for (VisualReadArc readArc: readArcs) {
				transform(visualModel, readArc);
			}
			visualModel.selectNone();
		}
	}

	@Override
	public void transform(Model model, Node node) {
		if ((model instanceof VisualModel) && (node instanceof VisualReadArc)) {
			VisualModel visualModel = (VisualModel)model;
			VisualReadArc readArc = (VisualReadArc)node;
			PetriNetUtils.replicateConnectedPlace(visualModel, readArc);
		}
	}

}
