package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.UUID;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
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
		File modelFile = null;
		try {
			final Framework framework = Framework.getInstance();
			modelFile = File.createTempFile("stg_", ".g");

			Model model = workspaceEntry.getModelEntry().getMathModel();
			UUID format = null;
			if (model instanceof PetriNetModel) {
				format = Format.STG;
			} else if (model instanceof Fsm) {
				format = Format.SG;
			}
			if (format == null) {
				return Result.exception(new Throwable("This tool is not applicable to " + model.getDisplayName() + " model."));
			}

			ExportTask exportTask = Export.createExportTask(model, modelFile, format, framework.getPluginManager());

			final Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, description +": writing .g");

			if (exportResult.getOutcome() != Outcome.FINISHED) {
				if (exportResult.getOutcome() == Outcome.CANCELLED) {
					return Result.cancelled();
				} else {
					return Result.exception(exportResult.getCause());
				}
			}

			PetrifyTask petrifyTask = new PetrifyTask(parameters, modelFile.getAbsolutePath());

			final Result<? extends ExternalProcessResult> petrifyResult
					= framework.getTaskManager().execute(petrifyTask, description + ": executing Petrify");

			if (petrifyResult.getOutcome() == Outcome.FINISHED) {
				try {
					ByteArrayInputStream in = new ByteArrayInputStream(petrifyResult.getReturnValue().getOutput());
					final STGModel outStg = new DotGImporter().importSTG(in);
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
		} finally {
			FileUtils.deleteFile(modelFile, CommonDebugSettings.getKeepTemporaryFiles());
		}
	}

}
