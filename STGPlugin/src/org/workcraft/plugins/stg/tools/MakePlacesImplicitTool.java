package org.workcraft.plugins.stg.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.WorkspaceEntry;

public class MakePlacesImplicitTool implements Tool {
	private final Framework framework;

	public MakePlacesImplicitTool(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getDisplayName() {
		return "Make places implicit";
	}

	@Override
	public String getSection() {
		return "Transformations";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<VisualPlace> places = new HashSet<VisualPlace>(stg.getVisualPlaces());
		if (!stg.getSelection().isEmpty()) {
			places.retainAll(stg.getSelection());
		}
		if (!places.isEmpty()) {
			we.saveMemento();
			for (VisualPlace place: places) {
				stg.maybeMakeImplicit(place);
			}
		}
	}

}
