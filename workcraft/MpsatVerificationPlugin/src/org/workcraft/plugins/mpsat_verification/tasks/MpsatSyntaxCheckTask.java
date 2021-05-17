package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class MpsatSyntaxCheckTask implements Task<ExternalProcessOutput> {

    private final VerificationParameters verificationParameters;
    private final File directory;

    public MpsatSyntaxCheckTask(VerificationParameters verificationParameters, File directory) {
        this.verificationParameters = verificationParameters;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
    }

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(MpsatVerificationSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        command.addAll(verificationParameters.getMpsatArguments(directory));

        // Option for exit early, after checking the predicate syntax
        command.add("-q");

        boolean printStdout = MpsatVerificationSettings.getPrintStdout();
        boolean printStderr = MpsatVerificationSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if (result.isSuccess() && (output != null)) {
            int returnCode = output.getReturnCode();
            if ((returnCode == 0) || (returnCode == 1)) {
                return Result.success(output);
            }
            return Result.failure(output);
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
