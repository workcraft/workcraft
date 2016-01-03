package org.workcraft.plugins.punf.tasks;

import java.util.ArrayList;

import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.ToolUtils;

public class PunfTask implements Task<ExternalProcessResult> {
	private String inputPath;
	private String outputPath;
	private boolean tryPnml;

	public PunfTask(String inputPath, String outputPath, boolean tryPnml) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.tryPnml = tryPnml;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
		ArrayList<String> command = new ArrayList<String>();

		// Name of the executable
		String toolPrefix = PunfUtilitySettings.getCommand();
		String toolSuffix = PunfUtilitySettings.getToolSuffix(tryPnml);
		String toolName = ToolUtils.getAbsoluteCommandWithSuffixPath(toolPrefix, toolSuffix);
		command.add(toolName);

		// Extra arguments (should go before the file parameters)
		for (String arg : PunfUtilitySettings.getExtraArgs().split(" ")) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}

		// Built-in arguments
		command.add("-m=" + outputPath);
		command.add(inputPath);

		Result<? extends ExternalProcessResult> res = new ExternalProcessTask(command).run(monitor);

		if (res.getOutcome() != Outcome.FINISHED) {
			return res;
		}

		ExternalProcessResult retVal = res.getReturnValue();
		if (retVal.getReturnCode() < 2) {
			return Result.finished(retVal);
		} else {
			return Result.failed(retVal);
		}
	}

}
