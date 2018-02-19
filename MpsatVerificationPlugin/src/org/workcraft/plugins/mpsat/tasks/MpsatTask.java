package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;

public class MpsatTask implements Task<MpsatOutput> {

    private static final Pattern patternSuccess = Pattern.compile(
            "(" +
             /* Deadlock */
            "the system is deadlock-free" +
            "|" +
            "the system has a deadlock" +
            "|" +
            /* CSC conflicts detection */
            "there are no CSC conflicts" +
            "|" +
            "CSC conflict has been detected" +
            "|" +
            /* USC conflicts detection */
            "there are no USC conflicts" +
            "|" +
            "USC conflict has been detected" +
            "|" +
            /* Normalcy */
            "there are no normalcy violations" +
            "|" +
            "normalcy is violated" +
            "|" +
            /* Reach expression */
            "no reachable state satisfies the predicate" +
            "|" +
            "there is a reachable state satisfying the predicate" +
            "|" +
            /* CSC resolution and decomposition */
            "all conflicts resolved \\(\\d+ signal insertion\\(s\\) and \\d+ concurrency reduction\\(s\\) applied\\)" +
            "|" +
            "no transformation computed" +
            "|" +
            "suggested a transformation" +
            ")",
            Pattern.UNIX_LINES);

    private final String[] args;
    private final File unfoldingFile;
    private final File directory;
    private final File netFile;
    private final File compFile;

    public MpsatTask(String[] args, File unfoldingFile, File directory) {
        this(args, unfoldingFile, directory, null, null);
    }

    public MpsatTask(String[] args, File unfoldingFile, File directory, File netFile) {
        this(args, unfoldingFile, directory, netFile, null);
    }

    public MpsatTask(String[] args, File unfoldingFile, File directory, File netFile, File compFile) {
        this.args = args;
        this.unfoldingFile = unfoldingFile;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
        this.netFile = netFile;
        this.compFile = compFile;
    }

    @Override
    public Result<? extends MpsatOutput> run(ProgressMonitor<? super MpsatOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolPrefix = MpsatSettings.getCommand();
        String unfoldingFileName = unfoldingFile.getName();
        String toolSuffix = unfoldingFileName.endsWith(PunfTask.MCI_FILE_EXTENSION) ? PunfTask.LEGACY_TOOL_SUFFIX : "";
        String toolName = ToolUtils.getAbsoluteCommandWithSuffixPath(toolPrefix, toolSuffix);
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = MpsatSettings.getArgs();
        if (MpsatSettings.getAdvancedMode()) {
            String tmp = DialogUtils.showInput("Additional parameters for MPSat:", extraArgs);
            if (tmp == null) {
                return Result.cancelation();
            }
            extraArgs = tmp;
        }
        for (String arg : extraArgs.split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Input file
        if (unfoldingFile != null) {
            command.add(unfoldingFile.getAbsolutePath());
        }

        boolean printStdout = MpsatSettings.getPrintStdout();
        boolean printStderr = MpsatSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        if (result.getOutcome() == Outcome.SUCCESS) {
            ExternalProcessOutput output = result.getPayload();
            int returnCode = output.getReturnCode();
            // Even if the return code is 0 or 1, still test MPSat output to make sure it has completed successfully.
            boolean success = false;
            if ((returnCode == 0) || (returnCode == 1)) {
                String stdout = new String(output.getStdout());
                Matcher matcherSuccess = patternSuccess.matcher(stdout);
                success = matcherSuccess.find();
            }
            if (!success) {
                return Result.failure(new MpsatOutput(output));
            } else {
                byte[] netInput = null;
                byte[] compInput = null;
                byte[] stgOutput = null;
                try {
                    if ((netFile != null) && netFile.exists()) {
                        netInput = FileUtils.readAllBytes(netFile);
                    }
                    if ((compFile != null) && compFile.exists()) {
                        compInput = FileUtils.readAllBytes(compFile);
                    }
                    File stgFile = new File(directory, MpsatOutput.STG_FILE_NAME);
                    if (stgFile.exists()) {
                        stgOutput = FileUtils.readAllBytes(stgFile);
                    }
                } catch (IOException e) {
                    return Result.exception(e);
                }

                return Result.success(new MpsatOutput(output, netInput, compInput, stgOutput));
            }
        }

        if (result.getOutcome() == Outcome.CANCEL) {
            return Result.cancelation();
        }

        return Result.failure(null);
    }

}
