package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TransformationTask implements Task<TransformationResult>, ExternalProcessListener {
	private WorkspaceEntry we;
	String[] args;

	public TransformationTask(WorkspaceEntry we, String description, String[] args) {
		this.we = we;
		this.args = args;
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return we;
	}

	@Override
	public Result<? extends TransformationResult> run(ProgressMonitor<? super TransformationResult> monitor) {
		ArrayList<String> command = new ArrayList<String>();

		// Name of the executable
		String toolName = ToolUtils.getAbsoluteCommandPath(PetrifyUtilitySettings.getPetrifyCommand());
		command.add(toolName);

		// Built-in arguments
		for (String arg : args) {
			command.add(arg);
		}

		// Extra arguments (should go before the file parameters)
		for (String arg : PetrifyUtilitySettings.getPetrifyArgs().split(" ")) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}

		String prefix = FileUtils.getTempPrefix(we.getTitle());
		File directory = FileUtils.createTempDirectory(prefix);
		try {
			File logFile = new File(directory, "petrify.log");
			command.add("-log");
			command.add(logFile.getAbsolutePath());

			File outFile = new File(directory, "result.g");
			command.add("-o");
			command.add(outFile.getAbsolutePath());

			// Input file
			Model model = we.getModelEntry().getMathModel();
			File modelFile = getInputFile(model, directory);
			command.add(modelFile.getAbsolutePath());

			boolean printStdout = PetrifyUtilitySettings.getPrintStdout();
			boolean printStderr = PetrifyUtilitySettings.getPrintStderr();
			ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);
			Result<? extends ExternalProcessResult> res = task.run(mon);

			if (res.getOutcome() == Outcome.CANCELLED) {
				return new Result<TransformationResult>(Outcome.CANCELLED);
			} else {
				final Outcome outcome;
				STGModel outStg = null;
				if (res.getReturnValue().getReturnCode() == 0) {
					outcome = Outcome.FINISHED;
				} else {
					outcome = Outcome.FAILED;
				}
				try {
					String out = (outFile.exists() ? FileUtils.readAllText(outFile) : "");
					ByteArrayInputStream outStream = new ByteArrayInputStream(out.getBytes());
					outStg = new DotGImporter().importSTG(outStream);
				} catch (DeserialisationException e) {
					return Result.exception(e);
				}
				TransformationResult result = new TransformationResult(res, outStg);
				return new Result<TransformationResult>(outcome, result);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
		}
	}

	private File getInputFile(Model model, File directory) {
		final Framework framework = Framework.getInstance();
		UUID format = null;
		String extension = null;
		if (model instanceof PetriNetModel) {
			format = Format.STG;
			extension = ".g";
		} else if (model instanceof Fsm) {
			format = Format.SG;
			extension = ".sg";
		}
		if (format == null) {
			throw new RuntimeException("This tool is not applicable to " + model.getDisplayName() + " model.");
		}

		File modelFile = new File(directory, "original" + extension);
		try {
			ExportTask exportTask = Export.createExportTask(model, modelFile, format, framework.getPluginManager());
			final Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Exporting model");
			if (exportResult.getOutcome() != Outcome.FINISHED) {
				modelFile = null;
			}
		} catch (SerialisationException e) {
			throw new RuntimeException("Unable to export the model.");
		}
		return modelFile;
	}


	@Override
	public void processFinished(int returnCode) {
	}

	@Override
	public void errorData(byte[] data) {
		System.out.print(data);
	}

	@Override
	public void outputData(byte[] data) {
		System.out.print(data);
	}

}
