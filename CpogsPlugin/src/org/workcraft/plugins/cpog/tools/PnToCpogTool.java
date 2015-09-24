package org.workcraft.plugins.cpog.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.cpog.tasks.PnToCpogHandler;
import org.workcraft.plugins.cpog.tasks.PnToCpogTask;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PnToCpogTool implements Tool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNet.class);
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public String getDisplayName() {
		return "Conditional Partial Order Graph";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();

		// Instantiate Solver
		PnToCpogTask task = new PnToCpogTask(we);
		// Instantiate object for handling solution
		PnToCpogHandler result = new PnToCpogHandler(task);
		//Run both
		framework.getTaskManager().queue(task, "Converting Petri net into CPOG...", result);
	}
}
