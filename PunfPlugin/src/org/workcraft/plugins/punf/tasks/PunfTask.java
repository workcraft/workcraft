package org.workcraft.plugins.punf.tasks;

import java.util.ArrayList;

import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.ToolUtils;

public class PunfTask implements Task<ExternalProcessResult> {
    private final String inputPath;
    private final String outputPath;
    private final boolean tryPnml;

    public PunfTask(String inputPath, String outputPath) {
        this(inputPath, outputPath, true);
    }

    public PunfTask(String inputPath, String outputPath, boolean tryPnml) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.tryPnml = tryPnml;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolPrefix = PunfSettings.getCommand();
        String toolSuffix = PunfSettings.getToolSuffix(tryPnml);
        String toolName = ToolUtils.getAbsoluteCommandWithSuffixPath(toolPrefix, toolSuffix);
        command.add(toolName);

        // Extra arguments (should go before the file parameters)
        for (String arg : PunfSettings.getArgs().split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Built-in arguments
        command.add("-m=" + outputPath);
        command.add(inputPath);

        boolean printStdout = PunfSettings.getPrintStdout();
        boolean printStderr = PunfSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, null, printStdout, printStderr);
        Result<? extends ExternalProcessResult> result = task.run(monitor);

        if (result.getOutcome() != Outcome.SUCCESS) {
            return result;
        }

        ExternalProcessResult returnValue = result.getReturnValue();
        int returnCode = returnValue.getReturnCode();
        if ((returnCode == 0) || (returnCode == 1)) {
            return Result.success(returnValue);
        } else {
            return Result.failure(returnValue);
        }
    }

}
