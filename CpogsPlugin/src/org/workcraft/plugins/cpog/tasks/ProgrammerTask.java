package org.workcraft.plugins.cpog.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cpog.ProgrammerUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;

public class ProgrammerTask implements Task<ExternalProcessResult> {
	private String[] args;
	private String inputFileName;

	public ProgrammerTask(String[] args, String inputFileName) {
		this.args = args;
		this.inputFileName = inputFileName;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {

		ArrayList<String> command = new ArrayList<String>();
		command.add(ProgrammerUtilitySettings.getCommand());

		for (String arg : ProgrammerUtilitySettings.getExtraArgs().split(" "))
			if (!arg.isEmpty())
				command.add(arg);

		for (String arg : args)
			command.add(arg);

		command.add(inputFileName);

		File workingDir = FileUtils.createTempDirectory("scenco");

		ExternalProcessTask externalProcessTask = new ExternalProcessTask(command, workingDir);

		Result<? extends ExternalProcessResult> res = externalProcessTask.run(monitor);

		if(res.getOutcome() == Outcome.CANCELLED)
			return res;

		Map<String, byte[]> outputFiles = new HashMap<String, byte[]>();

		try {
			File mci = new File(workingDir, "scenco.mci");
			if(mci.exists())
				outputFiles.put("scenco.mci", FileUtils.readAllBytes(mci));

			File g = new File(workingDir, "scenco.g");
			if(g.exists())
				outputFiles.put("scenco.g", FileUtils.readAllBytes(g));
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
