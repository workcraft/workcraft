package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCscResolutionResultHandler implements Runnable {

	private final MpsatChainTask task;
	private final Result<? extends MpsatChainResult> result;

	public MpsatCscResolutionResultHandler(MpsatChainTask task, Result<? extends MpsatChainResult> result) {
				this.task = task;
				this.result = result;
	}

	public STGModel getResolvedStg() {
		final byte[] output = result.getReturnValue().getMpsatResult().getReturnValue().getOutputFile("mpsat.g");
		if(output == null) {
			return null;
		}
		try {
			return new DotGImporter().importSTG(new ByteArrayInputStream(output));
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		final Framework framework = Framework.getInstance();
		final WorkspaceEntry we = task.getWorkspaceEntry();
		Path<String> path = we.getWorkspacePath();
		String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));

		STGModel model = getResolvedStg();
		if (model == null) {
			JOptionPane.showMessageDialog(framework.getMainWindow(),
					"MPSat output: \n\n" + new String(result.getReturnValue().getMpsatResult().getReturnValue().getErrors()),
					"Conflict resolution failed", JOptionPane.WARNING_MESSAGE );
		} else {
			Path<String> directory = path.getParent();
			String name = fileName + "_resolved";
			ModelEntry me = new ModelEntry(new StgDescriptor(), model);
			Workspace workspace = framework.getWorkspace();
			boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
			workspace.add(directory, name, me, true, openInEditor);
		}
	}
}
