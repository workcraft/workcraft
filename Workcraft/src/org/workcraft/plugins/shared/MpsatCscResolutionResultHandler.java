package org.workcraft.plugins.shared;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.shared.tasks.MpsatChainResult;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCscResolutionResultHandler implements Runnable {

	private final MpsatChainTask task;
	private final Result<? extends MpsatChainResult> mpsatChainResult;

	public MpsatCscResolutionResultHandler(MpsatChainTask task,
			Result<? extends MpsatChainResult> mpsatChainResult) {
				this.task = task;
				this.mpsatChainResult = mpsatChainResult;
	}

	@Override
	public void run() {
		WorkspaceEntry we = task.getWorkspaceEntry();
		Path<String> path = we.getWorkspacePath();
		String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));

		Model model;
		try {
			model = new DotGImporter().importFrom(new ByteArrayInputStream(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getOutputFile("mpsat.g")));
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}

		task.getFramework().getWorkspace().add(path.getParent(), fileName + "_resolved", model, true);
		;
	}

}
