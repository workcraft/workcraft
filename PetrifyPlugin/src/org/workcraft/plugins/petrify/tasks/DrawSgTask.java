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
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;

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
	private final Model model;

	public DrawSgTask(Model model) {
		this.model = model;
	}

	@Override
	public Result<? extends DrawSgResult> run(ProgressMonitor<? super DrawSgResult> monitor) {
		final Framework framework = Framework.getInstance();
		try	{
			File dotG = File.createTempFile("workcraft", ".g");
			dotG.deleteOnExit();

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

			File sg = File.createTempFile("workcraft", ".g");
			sg.deleteOnExit();

			List<String> writeSgOptions = new ArrayList<String>();
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
			File ps = File.createTempFile("workcraft", ".ps");
			ps.deleteOnExit();

			DrawAstgTask drawAstgTask = new DrawAstgTask(sg.getAbsolutePath(), ps.getAbsolutePath(), new ArrayList<String>());
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

			dotG.delete();
			sg.delete();
			return Result.finished(new DrawSgResult(ps, "No errors"));
		} catch (Throwable e) {
			e.printStackTrace();
			return Result.exception(e);
		}
	}

}
