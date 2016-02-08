package org.workcraft.plugins.petrify.tasks;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.petrify.PetrifyExtraUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.ToolUtils;
import org.workcraft.tasks.Task;

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
        String toolName = ToolUtils.getAbsoluteCommandPath(PetrifyExtraUtilitySettings.getDrawAstgCommand());
        command.add(toolName);

        // Built-in arguments
        for (String arg : options) {
            command.add(arg);
        }

        // Extra arguments (should go before the file parameters)
        for (String arg : PetrifyExtraUtilitySettings.getDrawAstgArgs().split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Input file
        command.add(inputPath);

        // Output file
        command.add("-o");
        command.add(outputPath);

        ExternalProcessTask task = new ExternalProcessTask(command, null);
        Result<? extends ExternalProcessResult> res = task.run(monitor);

        if (res.getOutcome() != Outcome.FINISHED)
            return res;

        ExternalProcessResult retVal = res.getReturnValue();
        if (retVal.getReturnCode() == 0)
            return Result.finished(retVal);
        else
            return Result.failed(retVal);
    }
}