package org.workcraft.plugins.cpog.tools;

import java.io.File;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.tasks.PGMinerResultHandler;
import org.workcraft.plugins.cpog.tasks.PGMinerTask;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class PGMinerTool implements Tool {

	protected boolean importAndExtract;

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		if (we.getModelEntry() == null) return false;
		if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}

	@Override
	public String getSection() {
		return "!Process Mining";
	}

	abstract public File getInputFile(WorkspaceEntry we) throws OperationCancelledException;


	@Override
	public void run(WorkspaceEntry we) {

		try {

			File inputFile = getInputFile(we);

			PGMinerTask task = new PGMinerTask(inputFile);

			final Framework framework = Framework.getInstance();
			PGMinerResultHandler result = new PGMinerResultHandler((VisualCPOG) we.getModelEntry().getVisualModel(), we, importAndExtract);
			framework.getTaskManager().queue(task, "PGMiner", result);
		} catch (ArrayIndexOutOfBoundsException | OperationCancelledException e) {

		}

	}


}
