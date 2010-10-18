package org.workcraft.plugins.mpsat;

import java.io.File;

import org.workcraft.plugins.gates.GateLevelModelDescriptor;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatSynthesisResultHandler implements Runnable {

	private final MpsatChainTask task;
	private final Result<? extends MpsatChainResult> mpsatChainResult;

	public MpsatSynthesisResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> mpsatChainResult) {
		this.task = task;
		this.mpsatChainResult = mpsatChainResult;
	}

	@Override
	public void run() {
		final WorkspaceEntry we = task.getWorkspaceEntry();
		final String desiredName = FileUtils.getFileNameWithoutExtension(new File(we.getWorkspacePath().getNode()));
		final String mpsatOutput = new String(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getOutput());
		task.getFramework().getWorkspace().add(we.getWorkspacePath().getParent(), desiredName, new ModelEntry(new GateLevelModelDescriptor(), new MpsatEqnParser().parse(mpsatOutput)), true);
	}
}
