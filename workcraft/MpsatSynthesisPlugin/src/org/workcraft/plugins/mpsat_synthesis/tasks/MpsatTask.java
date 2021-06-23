package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.utils.VerilogUtils;
import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.plugins.mpsat_synthesis.MpsatSynthesisSettings;
import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MpsatTask implements Task<MpsatOutput> {

    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    private static final String STG_FILE_NAME = "mpsat.g";
    private static final String VERILOG_FILE_NAME = "mpsat.v";
    private static final String CHECK_GATE_LIBRARY_MESSAGE =
            "Check '" + CircuitSettings.GATE_LIBRARY_TITLE + "' item in Digital Circuit preferences.";

    private static final Pattern SUCCESS_PATTERN = Pattern.compile(
            "(" +
            /* CSC resolution and decomposition */
            "all conflicts resolved \\(\\d+ signal insertion\\(s\\) and \\d+ concurrency reduction\\(s\\) applied\\)" +
            "|" +
            /* Synthesis and technology mapping */
            "Original Num Var/Cl/Lit\\s+\\d+/\\d+/\\d+\\R" +
            "\\s*SAT/Total time:\\s+(\\d+\\.)?\\d+/(\\d+\\.)?\\d+" +
            ")");

    private static final Pattern FAILURE_PATTERN = Pattern.compile(
            "Warning: failed to resolve some of the encoding conflicts\\R");

    private final File file;
    private final SynthesisMode mode;
    private final File directory;

    public MpsatTask(File file, SynthesisMode mode, File directory) {
        this.file = file;
        this.mode = mode;
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
        String toolName = ExecutableUtils.getAbsoluteCommandPath(MpsatSynthesisSettings.getCommand());
        command.add(toolName);

        String modeParameter = null;
        // Technology mapping library (if needed and accepted)
        if (mode == SynthesisMode.TECH_MAPPING) {
            String gateLibrary = ExecutableUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
            if ((gateLibrary == null) || gateLibrary.isEmpty()) {
                return Result.exception(new IOException("Gate library is not specified.\n" +
                        CHECK_GATE_LIBRARY_MESSAGE));
            }
            File gateLibraryFile = new File(gateLibrary);
            if (!FileUtils.checkAvailability(gateLibraryFile, "Gate library access error", false)) {
                return Result.exception(new IOException("Cannot find gate library file '" + gateLibrary + "'.\n" +
                        CHECK_GATE_LIBRARY_MESSAGE));
            }
            modeParameter = gateLibraryFile.getAbsolutePath();
        }

        // Built-in arguments
        command.addAll(mode.getMpsatArguments(modeParameter));

        // Global arguments
        if (MpsatSynthesisSettings.getReplicateSelfloopPlaces()) {
            command.add("-l");
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
        if (file != null) {
            command.add(file.getAbsolutePath());
        }

        // Output file
        File verilogFile = new File(directory, VERILOG_FILE_NAME);
        if (mode != SynthesisMode.RESOLVE_ENCODING_CONFLICTS) {
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
            // Even if the return code is 0 or 1, still test MPSat stdout and stderr to make sure it has completed successfully.
            boolean successFound = SUCCESS_PATTERN.matcher(output.getStdoutString()).find();
            boolean failureFound = FAILURE_PATTERN.matcher(output.getStderrString()).find();
            if ((returnCode != 0) && (returnCode != 1) || !successFound || failureFound) {
                return Result.failure(new MpsatOutput(output, null, null));
            }

            VerilogModule verilogModule = verilogFile.exists() ? VerilogUtils.importTopVerilogModule(verilogFile) : null;
            File stgFile = new File(directory, STG_FILE_NAME);
            Stg stg = stgFile.exists() ? StgUtils.importStg(stgFile) : null;
            return Result.success(new MpsatOutput(output, verilogModule, stg));
        }

        if (result.isCancel()) {
            return Result.cancel();
        }

        return Result.exception(result.getCause());
    }

}
