package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionContractorTool implements Tool {

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
		final VisualSTG model = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<VisualTransition> transitions = new HashSet<VisualTransition>(model.getVisualTransitions());
		transitions.retainAll(model.getSelection());
		if (!transitions.isEmpty()) {
			we.saveMemento();
			for (VisualTransition transition: transitions) {
				contractTransition(model, transition);
			}
			model.remove(new LinkedList<Node>(transitions));
		}
	}

	private void contractTransition(VisualSTG model, VisualTransition transition) {
		for (Node pred: model.getPreset(transition)) {
			for (Node succ: model.getPostset(transition)) {
				if ((pred instanceof VisualTransition) && (succ instanceof VisualTransition)) {
					try {
						VisualConnection oldConnection = (VisualConnection)model.getConnection(pred, transition);
						VisualConnection newConnection = model.connect(pred, succ);
						oldConnection.copyProperties(newConnection);
					} catch (InvalidConnectionException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
