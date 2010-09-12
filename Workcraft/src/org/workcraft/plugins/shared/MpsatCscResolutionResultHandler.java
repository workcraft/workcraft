package org.workcraft.plugins.shared;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.shared.tasks.MpsatChainResult;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STGModel;
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

	public STGModel getResolvedStg()
	{
		final byte[] output = mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getOutputFile("mpsat.g");
		if(output == null)
			return null;

		try {
			return new DotGImporter().importSTG(new ByteArrayInputStream(output));
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		WorkspaceEntry we = task.getWorkspaceEntry();
		Path<String> path = we.getWorkspacePath();
		String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));

		STGModel model = getResolvedStg();
		if (model == null)
		{
			JOptionPane.showMessageDialog(task.getFramework().getMainWindow(), "MPSat output: \n\n" + new String(mpsatChainResult.getReturnValue().getMpsatResult().getReturnValue().getErrors()), "Conflict resolution failed", JOptionPane.WARNING_MESSAGE );
		} else
		{
			final WorkspaceEntry resolved = task.getFramework().getWorkspace().add(path.getParent(), fileName + "_resolved", model, true);
			task.getFramework().getMainWindow().createEditorWindow(resolved);
		}
	}
}
