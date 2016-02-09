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

    public PunfTask(String inputPath, String outputPath) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        ArrayList<String> command = new ArrayList<String>();

        // Name of the executable
        String toolName = ToolUtils.getAbsoluteCommandPath(PunfUtilitySettings.getCommand());
        command.add(toolName);

        // Extra arguments (should go before the file parameters)
        for (String arg : PunfUtilitySettings.getExtraArgs().split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Built-in arguments
        command.add("-m=" + outputPath);
        command.add(inputPath);

        boolean printStdout = PunfUtilitySettings.getPrintStdout();
        boolean printStderr = PunfUtilitySettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, null, printStdout, printStderr);
        Result<? extends ExternalProcessResult> res = task.run(monitor);

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
