package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DrawSgTask implements Task<DrawSgResult> {

	private final class HugeSgRunnable implements Runnable {
		private final String stateCountMsg;
		private boolean hugeSgConfirmed = false;

		private HugeSgRunnable(String stateCountMsg) {
			this.stateCountMsg = stateCountMsg;
		}

		@Override
		public void run() {
			final Framework framework = Framework.getInstance();
			int answer = JOptionPane.showConfirmDialog(framework.getMainWindow(),
				"The state graph contains " + stateCountMsg + " states."
				+ "It may take a very long time to be processed.\n\n"
				+ "Are you sure you want to display it?",
				"Please confirm", JOptionPane.YES_NO_OPTION);
			hugeSgConfirmed = (answer == JOptionPane.YES_OPTION);
		}

		public boolean isHugeSgConfirmed() {
			return hugeSgConfirmed;
		}
	}

	final Pattern hugeSgPattern = Pattern.compile("with ([0-9]+) states");

	private final WorkspaceEntry we;
	private final boolean binary;

	public DrawSgTask(WorkspaceEntry we, boolean binary) {
		this.we = we;
		this.binary = binary;
	}

	@Override
	public Result<? extends DrawSgResult> run(ProgressMonitor<? super DrawSgResult> monitor) {
		final Framework framework = Framework.getInstance();
		File directory = null;
		try {
			String prefix = FileUtils.getTempPrefix(we.getTitle());
			directory = FileUtils.createTempDirectory(prefix);

			File dotG = new File(directory, "model.g");
			Model model = WorkspaceUtils.getAs(we, PetriNetModel.class);
			ExportTask exportTask = Export.createExportTask(model, dotG, Format.STG, framework.getPluginManager());
			final Result<? extends Object> dotGResult = framework.getTaskManager().execute(exportTask, "Exporting to .g" );

			if (dotGResult.getOutcome() != Outcome.FINISHED) {
				if (dotGResult.getOutcome() != Outcome.CANCELLED) {
					if (dotGResult.getCause() != null) {
						return Result.exception(dotGResult.getCause());
					} else {
						return Result.failed(new DrawSgResult(null, "Export to .g failed for unknown reason"));
					}
				}
				return Result.cancelled();
			}

			File sg = new File(directory, "model.sg");
			List<String> writeSgOptions = new ArrayList<String>();
			if (binary) {
				writeSgOptions.add("-bin");
			}
			while (true) {
				WriteSgTask writeSgTask = new WriteSgTask(dotG.getAbsolutePath(), sg.getAbsolutePath(), writeSgOptions);
				Result<? extends ExternalProcessResult> writeSgResult = framework.getTaskManager().execute(
						writeSgTask, "Running write_sg");

				if (writeSgResult.getOutcome() == Outcome.FINISHED) {
					break;
				}
				if (writeSgResult.getOutcome() == Outcome.CANCELLED) {
					return Result.cancelled();
				}
				if (writeSgResult.getCause() != null) {
					return Result.exception(writeSgResult.getCause());
				} else {
					final String errorMessages = new String(writeSgResult.getReturnValue().getErrors());
					final Matcher matcher = hugeSgPattern.matcher(errorMessages);
					if (matcher.find()) {
						final HugeSgRunnable hugeSgRunnable = new HugeSgRunnable(matcher.group(1));
						SwingUtilities.invokeAndWait(hugeSgRunnable);
						if (hugeSgRunnable.isHugeSgConfirmed()) {
							writeSgOptions.add("-huge");
							continue;
						} else {
							return Result.cancelled();
						}
					} else {
						return Result.failed(new DrawSgResult(null, errorMessages));
					}
				}
			}
			File ps = new File(directory, "model.ps");
			ArrayList<String> drawAstgOptions = new ArrayList<String>();
			if (binary) {
				drawAstgOptions.add("-bin");
			}
			DrawAstgTask drawAstgTask = new DrawAstgTask(sg.getAbsolutePath(), ps.getAbsolutePath(), drawAstgOptions);
			final Result<? extends ExternalProcessResult> drawAstgResult = framework.getTaskManager().execute(drawAstgTask, "Running draw_astg");

			if (drawAstgResult.getOutcome() != Outcome.FINISHED) {
				if (drawAstgResult.getOutcome() != Outcome.CANCELLED) {
					if (drawAstgResult.getCause() != null) {
						return Result.exception(drawAstgResult.getCause());
					} else {
						return Result.failed(new DrawSgResult(null, "Errors running draw_astg: \n"
							+ new String(drawAstgResult.getReturnValue().getErrors())));
					}
				}
				return Result.cancelled();
			}
			return Result.finished(new DrawSgResult(ps, "No errors"));
		} catch (Throwable e) {
			return Result.exception(e);
		} finally {
			FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
		}
	}

}
