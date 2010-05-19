package org.workcraft.plugins.shared.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.shared.MpsatUtilitySettings;
import org.workcraft.plugins.verification.tasks.ExternalProcessTask;
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
	public Result<ExternalProcessResult> run(ProgressMonitor<ExternalProcessResult> monitor) {

		ArrayList<String> command = new ArrayList<String>();
		command.add(MpsatUtilitySettings.getMpsatCommand());

		for (String arg : MpsatUtilitySettings.getMpsatArgs().split(" "))
			if (!arg.isEmpty())
				command.add(arg);

		for (String arg : args)
			command.add(arg);

		command.add(inputFileName);

		File workingDir = FileUtils.createTempDirectory("mpsat_");

		ExternalProcessTask externalProcessTask = new ExternalProcessTask(command, workingDir);

		Result<ExternalProcessResult> res = externalProcessTask.run(monitor);

		if(res.getOutcome() == Outcome.CANCELLED)
			return res;

		Map<String, byte[]> outputFiles = new HashMap<String, byte[]>();

		try {
			File mci = new File(workingDir, "mpsat.mci");
			if(mci.exists())
				outputFiles.put("mpsat.mci", FileUtils.readAllBytes(mci));

			File g = new File(workingDir, "mpsat.g");
			if(g.exists())
				outputFiles.put("mpsat.g", FileUtils.readAllBytes(g));
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