package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.mpsat_synthesis.MpsatSynthesisSettings;
import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.tasks.*;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MpsatTask implements Task<MpsatOutput> {

    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    private static final String STG_FILE_NAME = "mpsat.g";
    private static final String VERILOG_FILE_NAME = "mpsat.v";

    private static final Pattern SUCCESS_PATTERN = Pattern.compile(
            "(" +
            /* CSC resolution and decomposition */
            "all conflicts resolved \\(\\d+ signal insertion\\(s\\) and \\d+ concurrency reduction\\(s\\) applied\\)" +
            "|" +
            /* Synthesis and technology mapping */
            "Original Num Var/Cl/Lit\\s+\\d+/\\d+/\\d+\\R" +
            "\\s*SAT/Total time:\\s+(\\d+\\.)?\\d+/(\\d+\\.)?\\d+" +
            ")",
            Pattern.UNIX_LINES);

    private final File unfoldingFile;
    private final SynthesisMode synthesisMode;
    private final File directory;

    public MpsatTask(File unfoldingFile, SynthesisMode synthesisMode, File directory) {
        this.unfoldingFile = unfoldingFile;
        this.synthesisMode = synthesisMode;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
    }

    @Override
    public Result<? extends MpsatOutput> run(ProgressMonitor<? super MpsatOutput> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolPrefix = MpsatSynthesisSettings.getCommand();
        String toolSuffix = unfoldingFile.getName().endsWith(PunfTask.MCI_FILE_EXTENSION) ? PunfTask.LEGACY_TOOL_SUFFIX : "";
        String toolName = ExecutableUtils.getAbsoluteCommandWithSuffixPath(toolPrefix, toolSuffix);
        command.add(toolName);

        // Built-in arguments
        for (String arg : synthesisMode.getArguments()) {
            command.add(arg);
        }

        // Technology mapping library (if needed and accepted)
        String gateLibrary = ExecutableUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        if (synthesisMode.needGateLibrary()) {
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
                return Result.cancel();
            }
            extraArgs = tmp;
        }
        command.addAll(TextUtils.splitWords(extraArgs));

        // Input file
        if (unfoldingFile != null) {
            command.add(unfoldingFile.getAbsolutePath());
        }

        // Output file
        File verilogFile = new File(directory, VERILOG_FILE_NAME);
        if (synthesisMode != SynthesisMode.RESOLVE_ENCODING_CONFLICTS) {
            command.add(verilogFile.getAbsolutePath());
        }

        boolean printStdout = MpsatSynthesisSettings.getPrintStdout();
        boolean printStderr = MpsatSynthesisSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        SubtaskMonitor<? super ExternalProcessOutput> subtaskMonitor = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(subtaskMonitor);

        ExternalProcessOutput output = result.getPayload();
        if (result.isSuccess() && (output != null)) {
            int returnCode = output.getReturnCode();
            // Even if the return code is 0 or 1, still test MPSat output to make sure it has completed successfully.
            boolean success = false;
            if ((returnCode == 0) || (returnCode == 1)) {
                Matcher matcherSuccess = SUCCESS_PATTERN.matcher(output.getStdoutString());
                success = matcherSuccess.find();
            }
            if (success) {
                try {
                    byte[] verilogBytes = verilogFile.exists() ? FileUtils.readAllBytes(verilogFile) : null;
                    File stgFile = new File(directory, STG_FILE_NAME);
                    byte[] stgBytes = stgFile.exists() ? FileUtils.readAllBytes(stgFile) : null;
                    return Result.success(new MpsatOutput(output, verilogBytes, stgBytes));
                } catch (IOException e) {
                    return Result.exception(e);
                }
            }
            return Result.failure(new MpsatOutput(output, null, null));
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
