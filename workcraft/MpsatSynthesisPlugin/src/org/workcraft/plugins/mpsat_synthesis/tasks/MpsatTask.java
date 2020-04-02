package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.mpsat_synthesis.MpsatSynthesisSettings;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MpsatTask implements Task<MpsatOutput> {

    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    private static final String STG_FILE_NAME = "mpsat.g";
    private static final String VERILOG_FILE_NAME = "mpsat.v";

    private static final Pattern patternSuccess = Pattern.compile(
            "Original Num Var/Cl/Lit\\s+\\d+/\\d+/\\d+\\R" +
            "\\s*SAT/Total time:\\s+(\\d+\\.)?\\d+/(\\d+\\.)?\\d+",
            Pattern.UNIX_LINES);

    private final String[] args;
    private final String inputFileName;
    private final File directory;
    private final boolean needsGateLibrary;

    public MpsatTask(String[] args, String inputFileName, File directory, boolean needsGateLibrary) {
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
    public Result<? extends MpsatOutput> run(ProgressMonitor<? super MpsatOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(MpsatSynthesisSettings.getCommand());
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Technology mapping library (if needed and accepted)
        String gateLibrary = ExecutableUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        if (needsGateLibrary) {
            if ((gateLibrary == null) || gateLibrary.isEmpty()) {
                return Result.exception(new IOException("Gate library is not specified.\n" +
                        "Check '" + CircuitSettings.GATE_LIBRARY_TITLE + "' item in Digital Circuit preferences."));
            }
            File gateLibraryFile = new File(gateLibrary);
            if (!FileUtils.checkAvailability(gateLibraryFile, "Gate library access error", false)) {
                return Result.exception(new IOException("Cannot find gate library file '" + gateLibrary + "'.\n" +
                        "Check '" + CircuitSettings.GATE_LIBRARY_TITLE + "' item in Digital Circuit preferences."));
            }
            command.add("-d");
            command.add(gateLibraryFile.getAbsolutePath());
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
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        if (result.getOutcome() == Outcome.SUCCESS) {
            ExternalProcessOutput output = result.getPayload();
            if (output != null) {
                int returnCode = output.getReturnCode();
                // Even if the return code is 0 or 1, still test MPSat output to make sure it has completed successfully.
                boolean success = false;
                if ((returnCode == 0) || (returnCode == 1)) {
                    Matcher matcherSuccess = patternSuccess.matcher(output.getStdoutString());
                    success = matcherSuccess.find();
                }
                if (success) {
                    byte[] verilogOutput = null;
                    byte[] stgOutput = null;
                    try {
                        File stgFile = new File(directory, STG_FILE_NAME);
                        if (stgFile.exists()) {
                            stgOutput = FileUtils.readAllBytes(stgFile);
                        }
                        if (verilogFile.exists()) {
                            verilogOutput = FileUtils.readAllBytes(verilogFile);
                        }
                    } catch (IOException e) {
                        return Result.exception(e);
                    }
                    return Result.success(new MpsatOutput(output, stgOutput, verilogOutput));
                }
                return Result.failure(new MpsatOutput(output));
            }
        }

        if (result.getOutcome() == Outcome.CANCEL) {
            return Result.cancelation();
        }

        return Result.exception(result.getCause());
    }

}
