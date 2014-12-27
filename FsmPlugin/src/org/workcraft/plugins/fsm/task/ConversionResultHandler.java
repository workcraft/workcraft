package org.workcraft.plugins.fsm.task;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmModelDescriptor;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class ConversionResultHandler extends DummyProgressMonitor<ConversionResult> {
	private final ConversionTask task;

	public ConversionResultHandler(ConversionTask task) {
		this.task = task;
	}

	@Override
	public void finished(final Result<? extends ConversionResult> result, String description) {

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run() {
				WorkspaceEntry we = task.getWorkspaceEntry();
				Path<String> path = we.getWorkspacePath();

				String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
				final Framework framework = Framework.getInstance();

				if (result.getOutcome() == Outcome.FINISHED) {
					Fsm model = result.getReturnValue().getResult();
					final Workspace workspace = framework.getWorkspace();
					final Path<String> directory = path.getParent();
					final String name = fileName + "_transformed";
					final ModelEntry me = new ModelEntry(new FsmModelDescriptor() , model);
					workspace.add(directory, name, me, true, true);
				} else {
					MainWindow mainWindow = framework.getMainWindow();
					if (result.getCause() == null) {
						JOptionPane.showMessageDialog(mainWindow,
								"Petrify output: \n\n" + new String(result.getReturnValue().getPetrifyResult().getReturnValue().getErrors()),
								"Transformation failed", JOptionPane.WARNING_MESSAGE);
					} else {
						ExceptionDialog.show(mainWindow, result.getCause());
					}
				}
			}
		});
	}
}
