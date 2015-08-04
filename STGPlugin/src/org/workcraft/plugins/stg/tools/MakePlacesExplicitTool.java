package org.workcraft.plugins.stg.tools;

import java.util.HashSet;

import org.workcraft.TransformationTool;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.WorkspaceEntry;

public class MakePlacesExplicitTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Make places explicit";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<VisualImplicitPlaceArc> connections = new HashSet<VisualImplicitPlaceArc>(stg.getVisualImplicitPlaceArcs());
		if (!stg.getSelection().isEmpty()) {
			connections.retainAll(stg.getSelection());
		}
		if (!connections.isEmpty()) {
			we.saveMemento();
			for (VisualImplicitPlaceArc connection: connections) {
				stg.makeExplicit(connection);
			}
		}
	}

}
