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

public class ProxyReadArcPlaceTool extends TransformationTool implements NodeTransformer {

	@Override
	public String getDisplayName() {
		return "Create proxies for read-arc places (selected or all)";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNetModel;
	}

	@Override
	public boolean isApplicableTo(Node node) {
		if (node instanceof VisualReadArc) {
			VisualReadArc readArc = (VisualReadArc)node;
			return (readArc.getFirst() instanceof VisualPlace);
		}
		return false;
	}

	@Override
	public Position getPosition() {
		return Position.MIDDLE;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualModel model = we.getModelEntry().getVisualModel();
		HashSet<VisualReadArc> readArcs = PetriNetUtils.getVisualReadArcs(model);
		if ( !model.getSelection().isEmpty() ) {
			readArcs.retainAll(model.getSelection());
		}
		HashSet<VisualPlace> places = PetriNetUtils.getVisualPlaces(model);
		if ( !model.getSelection().isEmpty() ) {
			places.retainAll(model.getSelection());
		}
		for (VisualPlace place: places) {
			for (Connection connection: model.getConnections(place)) {
				if (connection instanceof VisualReadArc) {
					readArcs.add((VisualReadArc)connection);
				}
			}
		}

		if ( !readArcs.isEmpty() ) {
			we.saveMemento();
			for (VisualReadArc readArc: readArcs) {
				transform(model, readArc);
			}
			model.selectNone();
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
