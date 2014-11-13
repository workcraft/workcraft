package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;

public class MpsatTask implements Task<ExternalProcessResult> {
	private String[] args;
	private String inputFileName;

	public MpsatTask(String[] args, String inputFileName) {
		this.args = args;
		this.inputFileName = inputFileName;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {

		ArrayList<String> command = new ArrayList<String>();
		// Name of the executable
		command.add(MpsatUtilitySettings.getCommand() + MpsatUtilitySettings.getCommandSuffix());
		// Built-in arguments
		for (String arg : args) {
			command.add(arg);
		}
		// Extra arguments
		for (String arg : MpsatUtilitySettings.getExtraArgs().split("\\s")) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}
		// Input file argument
		command.add(inputFileName);

		File workingDir = FileUtils.createTempDirectory("mpsat_");
		ExternalProcessTask externalProcessTask = new ExternalProcessTask(command, workingDir);

		Result<? extends ExternalProcessResult> res = externalProcessTask.run(monitor);

		if(res.getOutcome() == Outcome.CANCELLED)
			return res;

		Map<String, byte[]> outputFiles = new HashMap<String, byte[]>();

		try {
			String unfoldingFileName = "mpsat" + MpsatUtilitySettings.getUnfoldingExtension();
			File unfoldingFile = new File(workingDir, unfoldingFileName);
			if(unfoldingFile.exists()) {
				outputFiles.put(unfoldingFileName, FileUtils.readAllBytes(unfoldingFile));
			}
			File g = new File(workingDir, "mpsat.g");
			if(g.exists()) {
				outputFiles.put("mpsat.g", FileUtils.readAllBytes(g));
			}
		} catch (IOException e) {
			return new Result<ExternalProcessResult>(e);
		}

		ExternalProcessResult retVal = res.getReturnValue();
		ExternalProcessResult result = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(), retVal.getErrors(), outputFiles);

		if (retVal.getReturnCode() < 2)
			return Result.finished(result);
		else
			return Result.failed(result);
	}
}