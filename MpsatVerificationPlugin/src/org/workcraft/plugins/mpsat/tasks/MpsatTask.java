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
    public static final String FILE_NET_G = "net.g";
    // IMPORTANT: The name of output file must be mpsat.g -- this is not configurable on MPSat side.
    public static final String FILE_MPSAT_G = "mpsat.g";
    public static final String FILE_PLACES = "places.list";

    private final String[] args;
    private final File unfoldingFile;
    private final File directory;
    private final boolean tryPnml;
    private final File netFile;
    private final File placesFile;

    public MpsatTask(String[] args, File unfoldingFile, File directory) {
        this(args, unfoldingFile, directory, true, null, null);
    }

    public MpsatTask(String[] args, File unfoldingFile, File directory, boolean tryPnml) {
        this(args, unfoldingFile, directory, tryPnml, null, null);
    }

    public MpsatTask(String[] args, File unfoldingFile, File directory, boolean tryPnml, File netFile) {
        this(args, unfoldingFile, directory, tryPnml, netFile, null);
    }

    public MpsatTask(String[] args, File unfoldingFile, File directory, boolean tryPnml, File netFile, File placesFile) {
        this.args = args;
        this.unfoldingFile = unfoldingFile;
        if (directory == null) {
            // Prefix must be at least 3 symbols long.
            directory = FileUtils.createTempDirectory("mpsat-");
        }
        this.directory = directory;
        this.tryPnml = tryPnml;
        this.netFile = netFile;
        this.placesFile = placesFile;
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
            if (tmp == null) {
                return Result.cancelled();
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

        boolean printStdout = MpsatUtilitySettings.getPrintStdout();
        boolean printStderr = MpsatUtilitySettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, directory, printStdout, printStderr);
        Result<? extends ExternalProcessResult> res = task.run(monitor);
        if (res.getOutcome() == Outcome.FINISHED) {
            Map<String, byte[]> fileContentMap = new HashMap<>();
            try {
                if ((netFile != null) && netFile.exists()) {
                    fileContentMap.put(FILE_NET_G, FileUtils.readAllBytes(netFile));
                }
                if ((placesFile != null) && placesFile.exists()) {
                    fileContentMap.put(FILE_PLACES, FileUtils.readAllBytes(placesFile));
                }
                File outFile = new File(directory, FILE_MPSAT_G);
                if (outFile.exists()) {
                    fileContentMap.put(FILE_MPSAT_G, FileUtils.readAllBytes(outFile));
                }
            } catch (IOException e) {
                return new Result<ExternalProcessResult>(e);
            }

            ExternalProcessResult retVal = res.getReturnValue();
            ExternalProcessResult result = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(), retVal.getErrors(), fileContentMap);
            if (retVal.getReturnCode() < 2) {
                return Result.finished(result);
            } else {
                return Result.failed(result);
            }
        }
        if (res.getOutcome() == Outcome.CANCELLED) {
            return Result.cancelled();
        }
        return Result.failed(null);
    }

}
