package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.ToolUtils;

public class PcompTask implements Task<PcompOutput> {

    public enum ConversionMode {
        DUMMY,
        INTERNAL,
        OUTPUT
    }

    private final File[] inputFiles;
    private final File outputFile;
    private final File detailsFile;
    private final ConversionMode conversionMode;
    private final boolean useSharedOutputs;
    private final boolean useImprovedComposition;
    private final File directory;

    public PcompTask(File[] inputFiles, File outputFile, File detailsFile,
            ConversionMode conversionMode, boolean useSharedOutputs, boolean useImprovedComposition, File directory) {
        this.inputFiles = inputFiles;
        this.outputFile = outputFile;
        this.detailsFile = detailsFile;
        this.conversionMode = conversionMode;
        this.useSharedOutputs = useSharedOutputs;
        this.useImprovedComposition = useImprovedComposition;
        this.directory = directory;
    }

    @Override
    public Result<? extends PcompOutput> run(ProgressMonitor<? super PcompOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ToolUtils.getAbsoluteCommandPath(PcompSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        if (conversionMode == ConversionMode.DUMMY) {
            command.add("-d");
            command.add("-r");
        } else if (conversionMode == ConversionMode.INTERNAL) {
            command.add("-i");
        }

        if (useSharedOutputs) {
            command.add("-o");
        }

        if (useImprovedComposition) {
            command.add("-p");
        }

        // Extra arguments (should go before the file parameters)
        for (String arg : PcompSettings.getArgs().split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Composed STG output file
        if (outputFile != null) {
            command.add("-f" + outputFile.getAbsolutePath());
        }

        // Composition details output file
        if (detailsFile != null) {
            command.add("-l" + detailsFile.getAbsolutePath());
        }

        // STG input files
        for (File inputFile: inputFiles) {
            if (inputFile != null) {
                command.add(inputFile.getAbsolutePath());
            }
        }

        ExternalProcessTask task = new ExternalProcessTask(command, directory, false, true);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);
        if (result.getOutcome() == Outcome.CANCEL) {
            return Result.cancelation();
        } else {
            ExternalProcessOutput output = result.getPayload();
            int returnCode = output.getReturnCode();
            if ((result.getOutcome() == Outcome.SUCCESS) && ((returnCode == 0) || (returnCode == 1))) {
                return Result.success(new PcompOutput(output));
            } else {
                return Result.failure(new PcompOutput(output));
            }
        }
    }

}
