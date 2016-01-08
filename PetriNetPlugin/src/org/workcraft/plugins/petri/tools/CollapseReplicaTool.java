package org.workcraft.plugins.petri.tools;

import java.util.HashSet;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.workspace.WorkspaceEntry;

public class CollapseReplicaTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Collapse replica places";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNetModel;
	}

	@Override
	public boolean isApplicableToNode(Node node) {
		return (node instanceof VisualReplicaPlace);
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualModel visualModel = we.getModelEntry().getVisualModel();
		HashSet<VisualReplicaPlace> replicas = PetriNetUtils.getVisualReplicaPlaces(visualModel);
		if ( !visualModel.getSelection().isEmpty() ) {
			replicas.retainAll(visualModel.getSelection());
		}

		HashSet<VisualReadArc> readArcs = PetriNetUtils.getVisualReadArcs(visualModel);
		if ( !visualModel.getSelection().isEmpty() ) {
			readArcs.retainAll(visualModel.getSelection());
		}
		if ( !readArcs.isEmpty() ) {
			for (VisualReadArc readArc: readArcs) {
				if (readArc.getFirst() instanceof VisualReplicaPlace) {
					VisualReplicaPlace replica = (VisualReplicaPlace)readArc.getFirst();
					replicas.add(replica);
				}
			}
		}
		if ( !replicas.isEmpty() ) {
			we.saveMemento();
			for (VisualReplicaPlace replica: replicas) {
				PetriNetUtils.collapseReplicaPlace(visualModel, replica);
			}
			visualModel.selectNone();
		}
	}

}
