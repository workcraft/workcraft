package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionContractorTool implements Tool {
	private final Framework framework;

	public TransitionContractorTool(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getDisplayName() {
		return "Contract selected transitions";
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
		HashSet<VisualTransition> transitions = new HashSet<VisualTransition>(stg.getVisualTransitions());
		if (!stg.getSelection().isEmpty()) {
			transitions.retainAll(stg.getSelection());
		}
		if (!transitions.isEmpty()) {
			we.saveMemento();
			for (VisualTransition transition: transitions) {
				contractTransition(stg, transition);
			}
			stg.remove(new LinkedList<Node>(transitions));
		}
	}

	private void contractTransition(VisualSTG stg, VisualTransition transition) {
		for (Node pred: stg.getPreset(transition)) {
			for (Node succ: stg.getPostset(transition)) {
				if ((pred instanceof VisualTransition) && (succ instanceof VisualTransition)) {
					try {
						VisualConnection oldConnection = (VisualConnection)stg.getConnection(pred, transition);
						VisualConnection newConnection = stg.connect(pred, succ);
						oldConnection.copyProperties(newConnection);
					} catch (InvalidConnectionException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
