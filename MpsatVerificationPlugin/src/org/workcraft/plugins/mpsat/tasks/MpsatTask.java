package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;

public class MpsatTask implements Task<ExternalProcessResult> {

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

    public static final String FILE_NET_G = "net.g";
    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    public static final String FILE_MPSAT_G = "mpsat.g";
    public static final String FILE_COMP_XML = "comp.xml";

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
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
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
        Result<? extends ExternalProcessResult> result = task.run(monitor);

        if (result.getOutcome() == Outcome.SUCCESS) {
            ExternalProcessResult returnValue = result.getReturnValue();
            int returnCode = returnValue.getReturnCode();
            // Even if the return code is 0 or 1, still test MPSat output to make sure it has completed successfully.
            boolean success = false;
            if ((returnCode == 0) || (returnCode == 1)) {
                String output = new String(returnValue.getOutput());
                Matcher matcherSuccess = patternSuccess.matcher(output);
                success = matcherSuccess.find();
            }
            if (!success) {
                return Result.failure(returnValue);
            } else {
                Map<String, byte[]> fileContentMap = new HashMap<>();
                try {
                    if ((netFile != null) && netFile.exists()) {
                        fileContentMap.put(FILE_NET_G, FileUtils.readAllBytes(netFile));
                    }
                    if ((compFile != null) && compFile.exists()) {
                        fileContentMap.put(FILE_COMP_XML, FileUtils.readAllBytes(compFile));
                    }
                    File outFile = new File(directory, FILE_MPSAT_G);
                    if (outFile.exists()) {
                        fileContentMap.put(FILE_MPSAT_G, FileUtils.readAllBytes(outFile));
                    }
                } catch (IOException e) {
                    return new Result<ExternalProcessResult>(e);
                }

                ExternalProcessResult extResult = new ExternalProcessResult(
                        returnCode, returnValue.getOutput(), returnValue.getErrors(), fileContentMap);

                return Result.success(extResult);
            }
        }

        if (result.getOutcome() == Outcome.CANCEL) {
            return Result.cancelation();
        }

        return Result.failure(null);
    }

}
