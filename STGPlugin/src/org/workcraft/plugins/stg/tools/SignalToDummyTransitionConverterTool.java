package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.workspace.WorkspaceEntry;

public class SignalToDummyTransitionConverterTool implements Tool {
	private final Framework framework;

	public SignalToDummyTransitionConverterTool(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getDisplayName() {
		return "Convert selected signal transitions to dummies";
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
		HashSet<VisualSignalTransition> signalTransitions = new HashSet<VisualSignalTransition>(stg.getVisualSignalTransitions());
		if (!stg.getSelection().isEmpty()) {
			signalTransitions.retainAll(stg.getSelection());
		}
		if (!signalTransitions.isEmpty()) {
			we.saveMemento();
			HashSet<VisualDummyTransition> dummyTransitions = new HashSet<VisualDummyTransition>(signalTransitions.size());
			for (VisualTransition transition: signalTransitions) {
				VisualDummyTransition dummyTransition = convertSignalToDummyTransition(stg, transition);
				dummyTransitions.add(dummyTransition);
			}
			stg.select(new LinkedList<Node>(dummyTransitions));
			stg.remove(new LinkedList<Node>(signalTransitions));
		}
	}

	private VisualDummyTransition convertSignalToDummyTransition(VisualSTG stg, VisualTransition signalTransition) {
		VisualDummyTransition dummyTransition = stg.createDummyTransition(null, null);
		dummyTransition.setPosition(signalTransition.getPosition());
		for (Node pred: stg.getPreset(signalTransition)) {
			for (Node succ: stg.getPostset(signalTransition)) {
				try {
					stg.connect(pred, dummyTransition);
					stg.connect(dummyTransition, succ);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}
		}
		return dummyTransition;
	}

}
