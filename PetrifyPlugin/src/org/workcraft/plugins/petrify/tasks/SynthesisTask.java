package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.petrify.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SynthesisTask implements Task<SynthesisResult>, ExternalProcessListener {
	private final WorkspaceEntry we;
	private final String[] args;

	/**
	 * @param args - arguments corresponding to type of logic synthesis
	 * @param inputFile - specification (STG)
	 * @param equationsFile - equation Output in EQN format (not BLIF format)
	 * @param libraryFile - could be null
	 * @param logFile - could be null
	 */
	public SynthesisTask(WorkspaceEntry we, String[] args) {
		this.we = we;
		this.args = args;
	}

	@Override
	public Result<? extends SynthesisResult> run(ProgressMonitor<? super SynthesisResult> monitor) {
		// Build the command line call for petrify.
		ArrayList<String> command = new ArrayList<String>();
		command.add(PetrifyUtilitySettings.getPetrifyCommand());

		for (String arg : PetrifyUtilitySettings.getPetrifyArgs().split(" ")) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}

		for (String arg : args) {
			command.add(arg);
		}

		String prefix = FileUtils.getTempPrefix(we.getTitle());
		File directory = FileUtils.createTempDirectory(prefix);
		try {
			File equationsFile = new File(directory, "petrify.eqn");
			command.add("-eqn");
			command.add(equationsFile.getCanonicalPath());

			File verilogFile = new File(directory, "petrify.v");
			command.add("-vl");
			command.add(verilogFile.getCanonicalPath());

			File logFile = new File(directory, "petrify.log");
			command.add("-log");
			command.add(logFile.getCanonicalPath());

			STGModel stg = WorkspaceUtils.getAs(we, STGModel.class);
			File stgFile = getInputSTG(stg, directory);
			command.add(stgFile.getCanonicalPath());

			// Call petrify on command line.
			ExternalProcessTask externalProcessTask = new ExternalProcessTask(command, directory);
			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);
			Result<? extends ExternalProcessResult> res = externalProcessTask.run(mon);

			if (res.getOutcome() == Outcome.CANCELLED) {
				return new Result<SynthesisResult>(Outcome.CANCELLED);
			} else {
				final Outcome outcome;
				if (res.getReturnValue().getReturnCode() == 0) {
					outcome = Outcome.FINISHED;
				} else {
					outcome = Outcome.FAILED;
				}

				String equations = FileUtils.readAllText(equationsFile);
				String verilog = FileUtils.readAllText(verilogFile);
				String log = FileUtils.readAllText(logFile);
				String stdout = new String(res.getReturnValue().getOutput());
				String stderr = new String(res.getReturnValue().getErrors());
				SynthesisResult result = new SynthesisResult(equations, verilog, log, stdout, stderr);
				return new Result<SynthesisResult>(outcome, result);
			}
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		} finally {
			FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
		}
	}

	private File getInputSTG(Model model, File directory) throws IOException {
		final Framework framework = Framework.getInstance();
		Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);
		if (stgExporter == null) {
			throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");
		}

		File stgFile = new File(directory, "petrify" + stgExporter.getExtenstion());
		ExportTask exportTask = new ExportTask(stgExporter, model, stgFile.getCanonicalPath());
		Result<? extends Object> res = framework.getTaskManager().execute(exportTask, "Exporting .g");
		if (res.getOutcome() != Outcome.FINISHED) {
			stgFile = null;
		}
		return stgFile;
	}

	@Override
	public void processFinished(int returnCode) {
	}

	@Override
	public void errorData(byte[] data) {
	}

	@Override
	public void outputData(byte[] data) {
	}

}
