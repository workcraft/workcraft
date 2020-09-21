package org.workcraft.plugins.punf.tasks;

import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;

public class PunfLtlxTask implements Task<PunfOutput> {

    private final File inputFile;
    private final File outputFile;
    private final File workingDir;

    public PunfLtlxTask(File inputFile, File outputFile, File workingDir) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.workingDir = workingDir;
    }

    @Override
    public Result<? extends PunfOutput> run(ProgressMonitor<? super PunfOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(PunfSettings.getCommand());
        command.add(toolName);

        // Extra arguments (should go before the file parameters)
        command.addAll(TextUtils.splitWords(PunfSettings.getArgs()));

        // Built-in arguments
        command.add("-L=" + outputFile.getAbsolutePath());
        command.add("-m");
        command.add("-c");
        command.add(inputFile.getAbsolutePath());

        boolean printStdout = PunfSettings.getPrintStdout();
        boolean printStderr = PunfSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, workingDir, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if (result.isSuccess() && (result.getOutcome() != null)) {
            int returnCode = output.getReturnCode();
            PunfOutput punfOutput = new PunfOutput(output, inputFile, outputFile);
            if ((returnCode == 0) || (returnCode == 1)) {
                return Result.success(punfOutput);
            }
            return Result.failure(punfOutput);
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
