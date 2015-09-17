package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.workspace.WorkspaceEntry;

public class DummyToSignalTransitionConverterTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Convert selected dummies to signal transitions";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<VisualDummyTransition> dummyTransitions = new HashSet<VisualDummyTransition>(stg.getVisualDummyTransitions());
		dummyTransitions.retainAll(stg.getSelection());
		if (!dummyTransitions.isEmpty()) {
			we.saveMemento();
			HashSet<VisualSignalTransition> signalTransitions = new HashSet<VisualSignalTransition>(dummyTransitions.size());
			for (VisualDummyTransition dummyTransition: dummyTransitions) {
				VisualSignalTransition signalTransition = StgUtils.convertDummyToSignalTransition(stg, dummyTransition);
				signalTransitions.add(signalTransition);
			}
			stg.select(new LinkedList<Node>(signalTransitions));
		}
	}

}
