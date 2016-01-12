package org.workcraft.plugins.stg.tools;

import java.util.HashSet;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.WorkspaceEntry;

public class MakePlacesImplicitTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Make places implicit";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public boolean isApplicableToNode(Node node) {
		return (node instanceof VisualPlace);
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<VisualPlace> places = new HashSet<>(stg.getVisualPlaces());
		if (!stg.getSelection().isEmpty()) {
			places.retainAll(stg.getSelection());
		}
		if (!places.isEmpty()) {
			we.saveMemento();
			for (VisualPlace place: places) {
				stg.maybeMakeImplicit(place, true);
			}
		}
	}

}
