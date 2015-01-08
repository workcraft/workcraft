package org.workcraft.plugins.fsm.task;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petrify.tasks.WriteSgTask;
import org.workcraft.plugins.shared.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.workspace.WorkspaceEntry;


public class ConversionTask implements Task<ConversionResult> {

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

	final private WorkspaceEntry we;
	final Pattern hugeSgPattern = Pattern.compile("with ([0-9]+) states");

	public ConversionTask(WorkspaceEntry we) {
		this.we = we;
	}

	@Override
	public Result<? extends ConversionResult> run(ProgressMonitor<? super ConversionResult> monitor) {
		final Framework framework = Framework.getInstance();
		File pnFile = null;
		try {
			// Common variables
			monitor.progressUpdate(0.05);
			PetriNet pn = (PetriNet)we.getModelEntry().getMathModel();
			pn.setTitle(we.getTitle());
			Exporter pnExporter = Export.chooseBestExporter(framework.getPluginManager(), pn, Format.STG);
			if (pnExporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + pn.getClass().getName() + " to format STG.");
			}
			SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<Object>(monitor);
			monitor.progressUpdate(0.10);

			// Generating .g file for Petri Net
			pnFile = File.createTempFile("stg_", ".g");
			ExportTask pnExportTask = new ExportTask(pnExporter, pn, pnFile.getCanonicalPath());
			Result<? extends Object> pnExportResult = framework.getTaskManager().execute(
					pnExportTask, "Exporting .g", subtaskMonitor);

			if (pnExportResult.getOutcome() != Outcome.FINISHED) {
				if (pnExportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<ConversionResult>(Outcome.CANCELLED);
				}
				return new Result<ConversionResult>(Outcome.FAILED);
			}
			monitor.progressUpdate(0.20);

			// Generate State Graph
			List<String> writeSgOptions = new ArrayList<String>();
			while (true) {
				WriteSgTask writeSgTask = new WriteSgTask(pnFile.getCanonicalPath(), null, writeSgOptions);
				Result<? extends ExternalProcessResult> writeSgResult = framework.getTaskManager().execute(
						writeSgTask, "Building state graph", subtaskMonitor);

				if (writeSgResult.getOutcome() == Outcome.FINISHED) {
//					try {
//						ByteArrayInputStream in = new ByteArrayInputStream(writeSgResult.getReturnValue().getOutput());
//						final Fsm fsm = new DotGImporter().importSG(in);
//						return Result.finished(new ConversionResult(null, fsm));
//					} catch (DeserialisationException e) {
//						return Result.exception(e);
//					}
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
						return Result.failed(new ConversionResult(writeSgResult, null));
					}
				}
			}

		} catch (Throwable e) {
			return new Result<ConversionResult>(e);
		} finally {
			if ((pnFile != null) && !PetrifyUtilitySettings.getDebugTemporaryFiles() ) {
				pnFile.delete();
			}
		}
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return we;
	}

}
