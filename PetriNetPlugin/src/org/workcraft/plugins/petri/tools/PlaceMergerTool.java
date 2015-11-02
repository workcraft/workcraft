package org.workcraft.plugins.petri.tools;

import java.util.Set;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.graph.tools.AbstractMergerTool;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.workspace.WorkspaceEntry;

public final class PlaceMergerTool extends AbstractMergerTool {
	@Override
	public String getDisplayName() {
		return "Merge selected places";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNetModel;
	}

	@Override
	public Set<Class<? extends VisualComponent>> getMergableClasses() {
		Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
		result.add(VisualPlace.class);
		return result;
	}

	@Override
	public <T extends VisualComponent> T createMergedComponent(VisualModel model, Set<VisualComponent> components, Class<T> type) {
		T result = super.createMergedComponent(model, components, type);
		int tokens = -1;
		for (VisualComponent component: components) {
			if (component instanceof VisualPlace) {
				VisualPlace place = (VisualPlace)component;
				int tmp = place.getReferencedPlace().getTokens();
				if (tokens < tmp) {
					tokens = tmp;
				}
			}
		}
		if (result instanceof VisualPlace) {
			((VisualPlace)result).getReferencedPlace().setTokens(tokens);
		}
		return result;
	}
}
