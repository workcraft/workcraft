package org.workcraft.plugins.mpsat;

import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.tasks.Result;

public class MpsatSynthesisResultHandler implements Runnable {

	private final MpsatChainTask task;
	private final Result<? extends MpsatChainResult> result;

	public MpsatSynthesisResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
		this.task = task;
		this.result = result;
	}

	@Override
	public void run() {
		final String mpsatOutput = new String(result.getReturnValue().getMpsatResult().getReturnValue().getOutput());
		System.out.println(mpsatOutput);
		// TODO: implement boolean function parsing into a digital circuit (beware of cyclic dependences)
		//final WorkspaceEntry we = task.getWorkspaceEntry();
		//final Path<String> directory = we.getWorkspacePath().getParent();
		//final String name = FileUtils.getFileNameWithoutExtension(new File(we.getWorkspacePath().getNode()));
		//final ModelEntry me = new ModelEntry(new CircuitModelDescriptor(), new MpsatEqnParser().parse(mpsatOutput));
		//workspace.add(directory, name, me, true, false);
	}
}
