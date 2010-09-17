package org.workcraft.plugins.petrify.tools;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.petrify.tasks.DrawSgResult;
import org.workcraft.plugins.petrify.tasks.DrawSgTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.workspace.handlers.SystemOpen;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ShowSg implements Tool {

	private final Framework framework;

	public ShowSg(Framework framework){
		this.framework = framework;
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public String getSection() {
		return "Misc.";
	}

	@Override
	public void run(WorkspaceEntry we) {
		DrawSgTask task = new DrawSgTask(WorkspaceUtils.getAs(we, STGModel.class), framework);
		framework.getTaskManager().queue(task, "Show state graph", new ProgressMonitor<DrawSgResult>() {
			@Override
			public void progressUpdate(double completion) {
			}

			@Override
			public void stdout(byte[] data) {
			}

			@Override
			public void stderr(byte[] data) {
			}

			@Override
			public boolean isCancelRequested() {
				return false;
			}

			@Override
			public void finished(Result<? extends DrawSgResult> result, String description) {

				if (result.getOutcome() == Outcome.FINISHED)
					SystemOpen.open(result.getReturnValue().getPsFile());
				else
					if (result.getOutcome() != Outcome.CANCELLED)
					{
						String errorMessage = "Petrify tool chain execution failed :-(";

						Throwable cause = result.getCause();

						if (cause != null)
							errorMessage += "\n\nFailure caused by: " + cause.toString() + "\nPlease see the \"Problems\" tab for more details.";
						else
							errorMessage += "\n\nFailure caused by: \n" + result.getReturnValue().getErrorMessages();

						final String err = errorMessage;

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								JOptionPane.showMessageDialog(null, err, "Oops..", JOptionPane.ERROR_MESSAGE);				}
						});
					}

			}
		});
	}

	@Override
	public String getDisplayName() {
		return "Show state graph (write_sg/draw_stg)";
	}
}
