package org.workcraft.plugins.petrify.tasks;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class TransformationResultHandler extends DummyProgressMonitor<TransformationResult> {
	private final TransformationTask task;

	public TransformationResultHandler(TransformationTask task) {
		this.task = task;
	}

	@Override
	public void finished(final Result<? extends TransformationResult> result, String description) {

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run() {
				WorkspaceEntry we = task.getWorkspaceEntry();
				Path<String> path = we.getWorkspacePath();

				String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));

				if (result.getOutcome() == Outcome.FINISHED)
				{
					STGModel model = result.getReturnValue().getResult();
					final WorkspaceEntry resolved = task.getFramework().getWorkspace().add(path.getParent(), fileName + "_transformed", new ModelEntry(new STGModelDescriptor() , model), true);
					task.getFramework().getMainWindow().createEditorWindow(resolved);
				} else
				{
					if (result.getCause() == null)
						JOptionPane.showMessageDialog(task.getFramework().getMainWindow(), "Petrify output: \n\n" + new String(result.getReturnValue().getPetrifyResult().getReturnValue().getErrors()), "Transformation failed", JOptionPane.WARNING_MESSAGE);
					else
						ExceptionDialog.show(task.getFramework().getMainWindow(), result.getCause());
				}
			}
		});
	}
}
