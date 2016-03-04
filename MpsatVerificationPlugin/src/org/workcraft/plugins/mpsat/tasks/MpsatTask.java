package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;

public class MpsatTask implements Task<ExternalProcessResult> {
    public static final String FILE_MPSAT_G = "mpsat.g";

    private final String[] args;
    private final String inputFileName;
    private final File directory;
    private final boolean tryPnml;

    public MpsatTask(String[] args, String inputFileName, File directory, boolean tryPnml) {
        this.args = args;
        this.inputFileName = inputFileName;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
        this.tryPnml = tryPnml;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolPrefix = MpsatUtilitySettings.getCommand();
        String toolSuffix = PunfUtilitySettings.getToolSuffix(tryPnml);
        String toolName = ToolUtils.getAbsoluteCommandWithSuffixPath(toolPrefix, toolSuffix);
        command.add(toolName);

        // Built-in arguments
        for (String arg : args) {
            command.add(arg);
        }

        // Extra arguments (should go before the file parameters)
        String extraArgs = MpsatUtilitySettings.getArgs();
        if (MpsatUtilitySettings.getAdvancedMode()) {
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            String tmp = JOptionPane.showInputDialog(mainWindow, "Additional parameters for MPSat:", extraArgs);
            if (tmp != null) {
                extraArgs = tmp;
            }
        }
        for (String arg : extraArgs.split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Input file
        command.add(inputFileName);

        boolean printStdout = MpsatUtilitySettings.getPrintStdout();
        boolean printStderr = MpsatUtilitySettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        Result<? extends ExternalProcessResult> res = task.run(monitor);
        if (res.getOutcome() == Outcome.CANCELLED) {
            return res;
        }

        Map<String, byte[]> outputFiles = new HashMap<>();
        try {
            File outFile = new File(directory, FILE_MPSAT_G);
            if (outFile.exists()) {
                outputFiles.put(FILE_MPSAT_G, FileUtils.readAllBytes(outFile));
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
