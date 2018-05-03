package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;

public class MpsatTask implements Task<MpsatOutput> {

    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    private static final String STG_FILE_NAME = "mpsat.g";

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

    public MpsatTask(String[] args, File unfoldingFile, File directory) {
        this(args, unfoldingFile, directory, null);
    }

    public MpsatTask(String[] args, File unfoldingFile, File directory, File netFile) {
        this.args = args;
        this.unfoldingFile = unfoldingFile;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
        this.netFile = netFile;
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
                StgModel inputStg = readStg(netFile);
                StgModel outputStg = readStg(new File(directory, STG_FILE_NAME));
                return Result.success(new MpsatOutput(output, inputStg, outputStg));
            }
        }

        if (result.getOutcome() == Outcome.CANCEL) {
            return Result.cancelation();
        }

        return Result.failure();
    }

    private StgModel readStg(File file) {
        if ((file != null) && file.exists()) {
            try {
                StgImporter importer = new StgImporter();
                FileInputStream is = new FileInputStream(file);
                return importer.importStg(is);
            } catch (final DeserialisationException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
