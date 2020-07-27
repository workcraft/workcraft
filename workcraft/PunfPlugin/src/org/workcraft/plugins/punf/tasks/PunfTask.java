package org.workcraft.plugins.punf.tasks;

import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;

public class PunfTask implements Task<PunfOutput> {

    public static final String PNML_FILE_EXTENSION = ".pnml";
    public static final String UNFOLDING_FILE_NAME = "unfolding" + PNML_FILE_EXTENSION;

    private final File inputFile;
    private final File outputFile;
    private final File directory;

    public PunfTask(File inputFile, File directory) {
        this(inputFile, new File(directory, UNFOLDING_FILE_NAME), directory);
    }

    public PunfTask(File inputFile, File outputFile, File directory) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.directory = directory;
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
        command.add("-m=" + outputFile.getAbsolutePath());
        command.add(inputFile.getAbsolutePath());

        boolean printStdout = PunfSettings.getPrintStdout();
        boolean printStderr = PunfSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if ((result.isSuccess()) && (output != null)) {
            PunfOutput punfOutput = new PunfOutput(output, outputFile);
            int returnCode = output.getReturnCode();
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
