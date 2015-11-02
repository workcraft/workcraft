package org.workcraft.plugins.fsm.tools;

import java.util.Set;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.graph.tools.AbstractMergerTool;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.workspace.WorkspaceEntry;

public final class StateMergerTool extends AbstractMergerTool {
	@Override
	public String getDisplayName() {
		return "Merge selected states";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Fsm;
	}

	@Override
	public Set<Class<? extends VisualComponent>> getMergableClasses() {
		Set<Class<? extends VisualComponent>> result = super.getMergableClasses();
		result.add(VisualState.class);
		return result;
	}

	@Override
	public <T extends VisualComponent> T createMergedComponent(VisualModel model, Set<VisualComponent> components, Class<T> type) {
		T result = super.createMergedComponent(model, components, type);
		boolean isFinal = false;
		boolean isInitial = false;
		for (VisualComponent component: components) {
			if (component instanceof VisualState) {
				VisualState state = (VisualState)component;
				if (state.getReferencedState().isFinal()) {
					isFinal = true;
				}
				if (state.getReferencedState().isInitial()) {
					isInitial = true;
				}
			}
		}
		if (result instanceof VisualState) {
			((VisualState)result).getReferencedState().setFinal(isFinal);
			((VisualState)result).getReferencedState().setInitial(isInitial);
		}
		return result;
	}
}
