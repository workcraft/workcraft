package org.workcraft.plugins.pcomp.tasks;

import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class PcompTask implements Task<PcompOutput> {

    private final Collection<File> inputFiles;
    private final File outputFile;
    private final File detailFile;
    private final PcompParameters parameters;
    private final File directory;

    public PcompTask(Collection<File> inputFiles, File outputFile, File detailFile,
            PcompParameters parameters, File directory) {

        this.inputFiles = inputFiles;
        this.outputFile = outputFile;
        this.detailFile = detailFile;
        this.parameters = parameters;
        this.directory = directory;
    }

    @Override
    public Result<? extends PcompOutput> run(ProgressMonitor<? super PcompOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(PcompSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        if (parameters.getSharedSignalMode() == PcompParameters.SharedSignalMode.DUMMY) {
            command.add("-d");
            command.add("-r");
        } else if (parameters.getSharedSignalMode() == PcompParameters.SharedSignalMode.INTERNAL) {
            command.add("-i");
        }

        if (parameters.isSharedOutputs()) {
            command.add("-o");
        }

        if (parameters.isImprovedComposition()) {
            command.add("-p");
        }

        // Extra arguments (should go before the file parameters)
        command.addAll(TextUtils.splitWords(PcompSettings.getArgs()));

        // Composed STG output file
        if (outputFile != null) {
            command.add("-f" + outputFile.getAbsolutePath());
        }

        // Composition detail output file
        if (detailFile != null) {
            command.add("-l" + detailFile.getAbsolutePath());
        }

        // STG input files
        for (File inputFile: inputFiles) {
            if (inputFile != null) {
                command.add(inputFile.getAbsolutePath());
            }
        }

        boolean printStdout = PcompSettings.getPrintStdout();
        boolean printStderr = PcompSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        if (result.getOutcome() == Outcome.SUCCESS) {
            ExternalProcessOutput output = result.getPayload();
            if (output != null) {
                int returnCode = output.getReturnCode();
                PcompOutput pcompOutput = new PcompOutput(output, inputFiles, outputFile, detailFile);
                if ((returnCode == 0) || (returnCode == 1)) {
                    return Result.success(pcompOutput);
                }
                return Result.failure(pcompOutput);
            }
        }

        if (result.getOutcome() == Outcome.CANCEL) {
            return Result.cancelation();
        }

        return Result.exception(result.getCause());
    }

}
