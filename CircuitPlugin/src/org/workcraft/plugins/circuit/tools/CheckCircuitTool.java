package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.tasks.CheckCircuitTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckCircuitTool implements Tool {

	public String getDisplayName() {
		return "Conformation, deadlock and hazard (reuse unfolding) [MPSat]";
	}

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Circuit;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final CheckCircuitTask task = new CheckCircuitTask(we, checkConformation(), checkDeadlock(), checkHazard());
		String description = "MPSat tool chain";
		String title = we.getModelEntry().getModel().getTitle();
		if (!title.isEmpty()) {
			description += "(" + title +")";
		}
		final Framework framework = Framework.getInstance();
		framework.getTaskManager().queue(task, description, new MpsatChainResultHandler(task));
	}

	public boolean checkConformation() {
		return true;
	}

	public boolean checkDeadlock() {
		return true;
	}

	public boolean checkHazard() {
		return true;
	}

}
