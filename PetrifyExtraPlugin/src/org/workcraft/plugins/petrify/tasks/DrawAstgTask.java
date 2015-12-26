package org.workcraft.plugins.petrify.tasks;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.petrify.PetrifyExtraUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.FileUtils;

public class DrawAstgTask implements Task<ExternalProcessResult> {
	private String inputPath, outputPath;
	private final List<String> options;

	public DrawAstgTask(String inputPath, String outputPath, List<String> options) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.options = options;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
		ArrayList<String> command = new ArrayList<String>();

		// Name of the executable
		String toolName = PetrifyExtraUtilitySettings.getDrawAstgCommand();
		if (PetrifyExtraUtilitySettings.getUseBundledVersion()) {
			toolName = FileUtils.getToolFileName(PetrifyExtraUtilitySettings.BUNDLED_DIRECTORY, toolName);
		}
		command.add(toolName);

		// Extra arguments
		for (String arg : PetrifyExtraUtilitySettings.getDrawAstgArgs().split(" ")) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}

		// Built-in arguments
		for (String arg : options) {
			command.add(arg);
		}
		command.add(inputPath);
		command.add("-o");
		command.add(outputPath);


		Result<? extends ExternalProcessResult> res = new ExternalProcessTask(command).run(monitor);

		if (res.getOutcome() != Outcome.FINISHED)
			return res;

		ExternalProcessResult retVal = res.getReturnValue();
		if (retVal.getReturnCode() == 0)
			return Result.finished(retVal);
		else
			return Result.failed(retVal);
	}
}