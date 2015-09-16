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

public class SignalToDummyTransitionConverterTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Convert selected signal transitions to dummies";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<VisualSignalTransition> signalTransitions = new HashSet<VisualSignalTransition>(stg.getVisualSignalTransitions());
		signalTransitions.retainAll(stg.getSelection());
		if (!signalTransitions.isEmpty()) {
			we.saveMemento();
			HashSet<VisualDummyTransition> dummyTransitions = new HashSet<VisualDummyTransition>(signalTransitions.size());
			for (VisualSignalTransition transition: signalTransitions) {
				VisualDummyTransition dummyTransition = StgUtils.convertSignalToDummyTransition(stg, transition);
				dummyTransitions.add(dummyTransition);
			}
			stg.select(new LinkedList<Node>(dummyTransitions));
		}
	}

}
