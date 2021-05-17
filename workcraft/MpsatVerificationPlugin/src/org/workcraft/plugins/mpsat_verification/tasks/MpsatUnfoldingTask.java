package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;

public class MpsatUnfoldingTask implements Task<MpsatOutput> {

    public static final String PNML_FILE_EXTENSION = ".pnml";
    public static final String UNFOLDING_FILE_NAME = "unfolding" + PNML_FILE_EXTENSION;

    private final File netFile;
    private final File unfoldingFile;
    private final File directory;

    public MpsatUnfoldingTask(File netFile, File directory) {
        this(netFile, new File(directory, UNFOLDING_FILE_NAME), directory);
    }

    public MpsatUnfoldingTask(File netFile, File unfoldingFile, File directory) {
        this.netFile = netFile;
        this.unfoldingFile = unfoldingFile;
        this.directory = directory;
    }

    @Override
    public Result<? extends MpsatOutput> run(ProgressMonitor<? super MpsatOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(MpsatVerificationSettings.getCommand());
        command.add(toolName);

        // Unfolding prefix mode and output file
        command.add("-Up");

        // Extra arguments (should go before the file parameters)
        command.addAll(TextUtils.splitWords(MpsatVerificationSettings.getArgs()));

        // Input net file
        command.add(netFile.getAbsolutePath());

        // Output unfolding file
        command.add(unfoldingFile.getAbsolutePath());

        boolean printStdout = MpsatVerificationSettings.getPrintStdout();
        boolean printStderr = MpsatVerificationSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if ((result.isSuccess()) && (output != null)) {
            MpsatOutput mpsatUnfoldingOutput = new MpsatOutput(output, null, netFile, unfoldingFile, null);
            int returnCode = output.getReturnCode();
            if ((returnCode == 0) || (returnCode == 1)) {
                return Result.success(mpsatUnfoldingOutput);
            }
            return Result.failure(mpsatUnfoldingOutput);
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
