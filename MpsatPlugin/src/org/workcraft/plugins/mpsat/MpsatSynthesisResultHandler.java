package org.workcraft.plugins.mpsat;

import java.io.File;

import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatSynthesisResultHandler implements Runnable {

	private final MpsatChainTask task;
	private final Result<? extends MpsatChainResult> result;

	public MpsatSynthesisResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
		this.task = task;
		this.result = result;
	}

	@Override
	public void run() {
		final Workspace workspace = task.getFramework().getWorkspace();
		final WorkspaceEntry we = task.getWorkspaceEntry();
		final String mpsatOutput = new String(result.getReturnValue().getMpsatResult().getReturnValue().getOutput());
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String name = FileUtils.getFileNameWithoutExtension(new File(we.getWorkspacePath().getNode()));
		final ModelEntry me = null;//
		// TODO: implement boolean function parsing into a digital circuit (beware of cyclic dependences)
		System.out.println("Parsing of Mpsat synthesis results is not implemented yet.");
		//final ModelEntry me = new ModelEntry(new CircuitModelDescriptor(), new MpsatEqnParser().parse(mpsatOutput));
		workspace.add(directory, name, me, true, false);
	}
}
