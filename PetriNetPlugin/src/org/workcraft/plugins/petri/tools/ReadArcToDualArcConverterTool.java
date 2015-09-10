package org.workcraft.plugins.petri.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class ReadArcToDualArcConverterTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Convert selected read-arc to a pair of producer-consumer arcs";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNetModel;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualModel visualModel = we.getModelEntry().getVisualModel();
		HashSet<VisualReadArc> readArcs = PetriNetUtils.getVisualReadArcs(visualModel);
		if ( !visualModel.getSelection().isEmpty() ) {
			readArcs.retainAll(visualModel.getSelection());
		}
		if ( !readArcs.isEmpty() ) {
			we.saveMemento();
			HashSet<VisualConnection> connections = new HashSet<>(2 * readArcs.size());
			for (VisualReadArc readArc: readArcs) {
				Pair<VisualConnection, VisualConnection> dualArc = PetriNetUtils.converReadArcTotDualArc(visualModel, readArc);
				VisualConnection consumingArc = dualArc.getFirst();
				if (consumingArc != null) {
					connections.add(consumingArc);
				}
				VisualConnection producingArc = dualArc.getSecond();
				if (producingArc != null) {
					connections.add(producingArc);
				}
			}
			visualModel.select(new LinkedList<Node>(connections));
		}
	}

}
