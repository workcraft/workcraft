package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.mpsat.MpsatSynthesisSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.ToolUtils;

public class MpsatSynthesisTask implements Task<ExternalProcessOutput> {

    private static final Pattern patternSuccess = Pattern.compile(
            "Original Num Var/Cl/Lit\\s+\\d+/\\d+/\\d+\\R" +
            "\\s*SAT/Total time:\\s+(\\d+\\.)?\\d+/(\\d+\\.)?\\d+",
            Pattern.UNIX_LINES);

    public static final String VERILOG_FILE_NAME = "mpsat.v";
    public static final String STG_FILE_NAME = "mpsat.g";

    private final String[] args;
    private final String inputFileName;
    private final File directory;
    private final boolean needsGateLibrary;

    public MpsatSynthesisTask(String[] args, String inputFileName, File directory, boolean needsGateLibrary) {
        this.args = args;
        this.inputFileName = inputFileName;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
        this.needsGateLibrary = needsGateLibrary;
    }

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ToolUtils.getAbsoluteCommandPath(MpsatSynthesisSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Technology mapping library (if needed and accepted)
        String gateLibrary = CircuitSettings.getGateLibrary();
        if (needsGateLibrary && (gateLibrary != null) && !gateLibrary.isEmpty()) {
            File gateLibraryFile = new File(gateLibrary);
            if (gateLibraryFile.exists()) {
                command.add("-d");
                command.add(gateLibraryFile.getAbsolutePath());
            } else {
                LogUtils.logWarning("Cannot find gate library file '" + gateLibrary + "'. Using built-in gate library of MPSat.");
            }
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = MpsatSynthesisSettings.getArgs();
        if (MpsatSynthesisSettings.getAdvancedMode()) {
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
        command.add(inputFileName);

        // Output file
        File verilogFile = new File(directory, VERILOG_FILE_NAME);
        command.add(verilogFile.getAbsolutePath());

        boolean printStdout = MpsatSynthesisSettings.getPrintStdout();
        boolean printStderr = MpsatSynthesisSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        Result<? extends ExternalProcessOutput> result = task.run(monitor);

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
                return Result.failure(output);
            } else {
                Map<String, byte[]> fileContentMap = new HashMap<>();
                try {
                    if (verilogFile.exists()) {
                        fileContentMap.put(VERILOG_FILE_NAME, FileUtils.readAllBytes(verilogFile));
                    }
                    File stgFile = new File(directory, STG_FILE_NAME);
                    if (stgFile.exists()) {
                        fileContentMap.put(STG_FILE_NAME, FileUtils.readAllBytes(stgFile));
                    }
                } catch (IOException e) {
                    return new Result<ExternalProcessOutput>(e);
                }

                ExternalProcessOutput extendedOutput = new ExternalProcessOutput(
                        returnCode, output.getStdout(), output.getStderr(), fileContentMap);

                return Result.success(extendedOutput);
            }
        }

        if (result.getOutcome() == Outcome.CANCEL) {
            return Result.cancelation();
        }

        return Result.failure(null);
    }

}
