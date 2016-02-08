package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.mpsat.MpsatSynthesisUtilitySettings;
import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.ToolUtils;

public class MpsatSynthesisTask implements Task<ExternalProcessResult> {
    public static final String EQN_FILE_NAME = "mpsat.eqn";
    public static final String VERILOG_FILE_NAME = "mpsat.v";

    private final String[] args;
    private final String inputFileName;
    private final File directory;
    private final boolean tryPnml;
    private final boolean needsGateLibrary;

    public MpsatSynthesisTask(String[] args, String inputFileName, File directory, boolean tryPnml, boolean needsGateLibrary) {
        this.args = args;
        this.inputFileName = inputFileName;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
        this.tryPnml = tryPnml;
        this.needsGateLibrary = needsGateLibrary;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolPrefix = MpsatSynthesisUtilitySettings.getCommand();
        String toolSuffix = PunfUtilitySettings.getToolSuffix(tryPnml);
        String toolName = ToolUtils.getAbsoluteCommandWithSuffixPath(toolPrefix, toolSuffix);
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Can this MPSat output Verilog?
        boolean canOutputVerilog = tryPnml && PunfUtilitySettings.getUsePnmlUnfolding();

        // Technology mapping library (if needed and accepted)
        if (canOutputVerilog && needsGateLibrary) {
            String gateLibrary = CircuitSettings.getGateLibrary();
            if ((gateLibrary != null) && !gateLibrary.isEmpty()) {
                File gateLibraryFile = new File(gateLibrary);
                if (gateLibraryFile.exists()) {
                    command.add("-d");
                    command.add(gateLibraryFile.getAbsolutePath());
                } else {
                    LogUtils.logWarningLine("Cannot find gate library file '" + gateLibrary + "'. Using built-in gate library of MPSat.");
                }
            }
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = MpsatSynthesisUtilitySettings.getArgs();
        if (MpsatSynthesisUtilitySettings.getAdvancedMode()) {
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            String tmp = JOptionPane.showInputDialog(mainWindow, "Additional parameters for MPSat:", extraArgs);
            if (tmp != null) {
                extraArgs = tmp;
            }
        }
        for (String arg : extraArgs.split("\\s")) {
            if ( !arg.isEmpty() ) {
                command.add(arg);
            }
        }

        // Input file
        command.add(inputFileName);

        // Output file
        String outputFileName = (canOutputVerilog ? VERILOG_FILE_NAME : EQN_FILE_NAME);
        File outputFile = new File(directory, outputFileName);
        command.add(outputFile.getAbsolutePath());

        boolean printStdout = MpsatSynthesisUtilitySettings.getPrintStdout();
        boolean printStderr = MpsatSynthesisUtilitySettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        Result<? extends ExternalProcessResult> res = task.run(monitor);
        if(res.getOutcome() == Outcome.CANCELLED) {
            return res;
        }

        Map<String, byte[]> outputFiles = new HashMap<String, byte[]>();
        try {
            File g = new File(directory, outputFileName);
            if(g.exists()) {
                outputFiles.put(outputFileName, FileUtils.readAllBytes(g));
            }
        } catch (IOException e) {
            return new Result<ExternalProcessResult>(e);
        }

        ExternalProcessResult retVal = res.getReturnValue();
        ExternalProcessResult result = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(), retVal.getErrors(), outputFiles);
        if (retVal.getReturnCode() < 2) {
            return Result.finished(result);
        } else {
            return Result.failed(result);
        }
    }

}
