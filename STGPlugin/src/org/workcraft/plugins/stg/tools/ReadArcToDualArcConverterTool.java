package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualReadArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class ReadArcToDualArcConverterTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Convert selected read-arc to a pair of producer-consumer arcs";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<VisualReadArc> readArcs = new HashSet<>(stg.getVisualReadArcs());
		if ( !stg.getSelection().isEmpty() ) {
			readArcs.retainAll(stg.getSelection());
		}
		if ( !readArcs.isEmpty() ) {
			we.saveMemento();
			HashSet<VisualConnection> connections = new HashSet<>(2 * readArcs.size());
			for (VisualReadArc readArc: readArcs) {
				Pair<VisualConnection, VisualConnection> dualArc = StgTransformationUtils.converReadArcTotDualArc(stg, readArc);
				VisualConnection consumingArc = dualArc.getFirst();
				if (consumingArc != null) {
					connections.add(consumingArc);
				}
				VisualConnection producingArc = dualArc.getSecond();
				if (producingArc != null) {
					connections.add(producingArc);
				}
			}
			stg.select(new LinkedList<Node>(connections));
		}
	}

}
