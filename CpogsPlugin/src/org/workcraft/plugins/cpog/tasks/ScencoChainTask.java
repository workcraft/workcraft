package org.workcraft.plugins.cpog.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoChainTask implements Task<ScencoChainResult> {

	private final WorkspaceEntry we;
	private final EncoderSettings settings;
	private VisualModel model;

	public ScencoChainTask(WorkspaceEntry we, EncoderSettings settings) {
		this.we = we;
		this.settings = settings;
		this.model = null;
	}

	@Override
	public Result<? extends ScencoChainResult> run(ProgressMonitor<? super ScencoChainResult> monitor) {
		final Framework framework = Framework.getInstance();
		try {
			if(model == null) {
				model = WorkspaceUtils.getAs(getWorkspaceEntry(), VisualModel.class);
			}
			Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.DOT);
			if (exporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");
			}
			File netFile = File.createTempFile("net", exporter.getExtenstion());
			ExportTask exportTask;
			exportTask = new ExportTask(exporter, model, netFile.getCanonicalPath());
			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);
			Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g", mon);
			if (exportResult.getOutcome() != Outcome.FINISHED) {
				netFile.delete();
				if (exportResult.getOutcome() == Outcome.CANCELLED)
					return new Result<ScencoChainResult>(Outcome.CANCELLED);
				return new Result<ScencoChainResult>(Outcome.FAILED, new ScencoChainResult(exportResult, null, settings));
			}
			monitor.progressUpdate(0.33);


			monitor.progressUpdate(1.0);

			return new Result<ScencoChainResult>(Outcome.FINISHED, null);
		} catch (Throwable e) {
			return new Result<ScencoChainResult>(e);
		}
	}

	public EncoderSettings getSettings() {
		return settings;
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return we;
	}

}
