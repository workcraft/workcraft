package org.workcraft.plugins.wtg.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.wtg.WaverSettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.DataAccumulator;
import org.workcraft.util.ToolUtils;

public class WaverTask implements Task<ExternalProcessOutput>, ExternalProcessListener {
    private final File inputFile;
    private final File outputFile;
    private final File workingDirectory;

    private ProgressMonitor<? super ExternalProcessOutput> monitor;

    private final DataAccumulator stdoutAccum = new DataAccumulator();
    private final DataAccumulator stderrAccum = new DataAccumulator();

    public WaverTask(File inputFile, File outputFile, File workingDirectory) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
        this.monitor = monitor;
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ToolUtils.getAbsoluteCommandPath(WaverSettings.getCommand());
        command.add(toolName);

        // Extra arguments (should go before the file parameters)
        for (String arg : WaverSettings.getArgs().split("\\s")) {
            if (!arg.isEmpty()) {
                command.add(arg);
            }
        }

        // Input file
        if (inputFile != null) {
            command.add(inputFile.getAbsolutePath());
        }

        // Output file
        if (outputFile != null) {
            command.add("-o");
            command.add(outputFile.getAbsolutePath());
        }

        boolean printStdout = WaverSettings.getPrintStdout();
        boolean printStderr = WaverSettings.getPrintStderr();
        ExternalProcessTask task = new ExternalProcessTask(command, workingDirectory, printStdout, printStderr);
        Result<? extends ExternalProcessOutput> res = task.run(monitor);
        if (res.getOutcome() != Outcome.SUCCESS) {
            return res;
        }

        ExternalProcessOutput retVal = res.getPayload();
        if (retVal.getReturnCode() == 0) {
            return Result.success(retVal);
        } else {
            return Result.failure(retVal);
        }
    }

    @Override
    public void errorData(byte[] data) {
        try {
            stderrAccum.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        monitor.stderr(data);
    }

    @Override
    public void outputData(byte[] data) {
        try {
            stdoutAccum.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        monitor.stdout(data);
    }

    @Override
    public void processFinished(int returnCode) {
    }

}
