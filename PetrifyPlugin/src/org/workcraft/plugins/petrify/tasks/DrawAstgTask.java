package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.shared.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;

public class DrawAstgTask implements Task<ExternalProcessResult> {
	private String inputPath, outputPath;
	private final List<String> options;

	public DrawAstgTask(String inputPath, String outputPath, List<String> options)
	{
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.options = options;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor)
	{
		ArrayList<String> command = new ArrayList<String>();
		command.add(PetrifyUtilitySettings.getDrawAstgCommand());

		for (String arg : PetrifyUtilitySettings.getDrawAstgArgs().split(" "))
			if (!arg.isEmpty())
				command.add(arg);

		for (String arg : options)
			command.add(arg);

		command.add(inputPath);
		command.add("-o");
		command.add(outputPath);


		Result<? extends ExternalProcessResult> res = new ExternalProcessTask(command, new File(".")).run(monitor);

		if (res.getOutcome() != Outcome.FINISHED)
			return res;

		ExternalProcessResult retVal = res.getReturnValue();
		if (retVal.getReturnCode() == 0)
			return Result.finished(retVal);
		else
			return Result.failed(retVal);
	}
}