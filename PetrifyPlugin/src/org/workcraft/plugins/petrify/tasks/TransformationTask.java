package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TransformationTask implements Task<TransformationResult>{
	private WorkspaceEntry workspaceEntry;
	String description;
	String[] parameters;

	public TransformationTask(WorkspaceEntry workspaceEntry, String description, String[] parameters) {
		this.workspaceEntry = workspaceEntry;
		this.description = description;
		this.parameters = parameters;
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return workspaceEntry;
	}

	@Override
	public Result<? extends TransformationResult> run(ProgressMonitor<? super TransformationResult> monitor) {
		try {
			final Framework framework = Framework.getInstance();
			File tmp = File.createTempFile("stg_", ".g");

			STGModel inStg = WorkspaceUtils.getAs(workspaceEntry, STGModel.class);
			ExportTask exportTask = Export.createExportTask(inStg, tmp, Format.STG, framework.getPluginManager());

			final Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, description +": writing .g");

			if (exportResult.getOutcome() != Outcome.FINISHED)
				if (exportResult.getOutcome() == Outcome.CANCELLED)
					return Result.cancelled();
				else
					return Result.exception(exportResult.getCause());

			PetrifyTask petrifyTask = new PetrifyTask(parameters, tmp.getAbsolutePath());

			final Result<? extends ExternalProcessResult> petrifyResult = framework.getTaskManager().execute(petrifyTask, description + ": executing Petrify");

			if (petrifyResult.getOutcome() == Outcome.FINISHED) {
				try {
					final STGModel outStg = new DotGImporter().importSTG(new ByteArrayInputStream(petrifyResult.getReturnValue().getOutput()));
					return Result.finished(new TransformationResult(null, outStg));
				} catch (DeserialisationException e) {
					return Result.exception(e);
				}

			} else {
				if(petrifyResult.getOutcome() == Outcome.FAILED) {
					return Result.failed(new TransformationResult(petrifyResult, null));
				} else {
					return Result.cancelled();
				}
			}
		} catch (Throwable e) {
			return Result.exception(e);
		}
	}

}
