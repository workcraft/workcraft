package org.workcraft.plugins.fsm.task;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.interop.DotGImporter;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petrify.tasks.TransformationResult;
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
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class ConversionTask implements Task<ConversionResult> {
	final private WorkspaceEntry we;

	public ConversionTask(WorkspaceEntry we) {
		this.we = we;
	}

	@Override
	public Result<? extends ConversionResult> run(ProgressMonitor<? super ConversionResult> monitor) {
		Framework framework = Framework.getInstance();
		File workingDirectory = null;
		try {
			// Common variables
			monitor.progressUpdate(0.05);
			String title = we.getWorkspacePath().getNode();
			if (title.endsWith(".work")) {
				title = title.substring(0, title.length() - 5);
			}

			workingDirectory = FileUtils.createTempDirectory(title + "-");

			PetriNet pn = (PetriNet)we.getModelEntry().getMathModel();
			Exporter pnExporter = Export.chooseBestExporter(framework.getPluginManager(), pn, Format.STG);
			if (pnExporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + pn.getClass().getName() + " to format STG.");
			}
			SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<Object>(monitor);
			monitor.progressUpdate(0.10);

			// Generating .g file for Petri Net
			String pnName = "pn.g";
			File pnFile =  new File(workingDirectory, pnName);
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

			// Generate .g file for State Graph
			File sgFile = new File(workingDirectory, "sg.g");
			WriteSgTask writeSgTask = new WriteSgTask(pnFile.getCanonicalPath(), sgFile.getCanonicalPath(), new ArrayList<String>());
			Result<? extends ExternalProcessResult> writeSgResult = framework.getTaskManager().execute(
					writeSgTask, "Building state graph", subtaskMonitor);

			if (writeSgResult.getOutcome() == Outcome.FINISHED) {
				try {
					final Fsm fsm = new DotGImporter().importSG(new ByteArrayInputStream(writeSgResult.getReturnValue().getOutput()));
					return Result.finished(new ConversionResult(null, fsm));
				} catch (DeserialisationException e) {
					return Result.exception(e);
				}
			} else {
				if (writeSgResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<ConversionResult>(Outcome.CANCELLED);
				}
				return new Result<ConversionResult>(Outcome.FAILED);
			}

		} catch (Throwable e) {
			return new Result<ConversionResult>(e);
		} finally {
			if ((workingDirectory != null) && !PetrifyUtilitySettings.getDebugTemporaryFiles()) {
				FileUtils.deleteDirectoryTree(workingDirectory);
			}
		}
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return we;
	}

}
