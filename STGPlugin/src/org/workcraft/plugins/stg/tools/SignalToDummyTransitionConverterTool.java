package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Tool;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.workspace.WorkspaceEntry;

public class SignalToDummyTransitionConverterTool implements Tool {

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
		signalTransitions.retainAll(stg.getSelection());
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
		Container container = (Container)signalTransition.getParent();
		VisualDummyTransition dummyTransition = stg.createDummyTransition(null, container);
		dummyTransition.setPosition(signalTransition.getPosition());
		for (Node pred: stg.getPreset(signalTransition)) {
			try {
				VisualConnection oldPredConnection = (VisualConnection)stg.getConnection(pred, signalTransition);
				VisualConnection newPredConnection = stg.connect(pred, dummyTransition);
				newPredConnection.copyStyle(oldPredConnection);
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}
		for (Node succ: stg.getPostset(signalTransition)) {
			try {
				VisualConnection oldSuccConnection = (VisualConnection)stg.getConnection(signalTransition, succ);
				VisualConnection newSuccConnection = stg.connect(dummyTransition, succ);
				newSuccConnection.copyStyle(oldSuccConnection);
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}
		return dummyTransition;
	}

}
