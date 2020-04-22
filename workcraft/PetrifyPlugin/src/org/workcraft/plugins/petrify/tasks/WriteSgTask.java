package org.workcraft.plugins.petrify.tasks;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.shared.DataAccumulator;
import org.workcraft.tasks.*;
import org.workcraft.utils.ExecutableUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteSgTask implements Task<ExternalProcessOutput>, ExternalProcessListener {

    private final List<String> options;
    private final File inputFile;
    private final File outputFile;
    private final File directory;

    private ProgressMonitor<? super ExternalProcessOutput> monitor;

    private final DataAccumulator stdoutAccum = new DataAccumulator();
    private final DataAccumulator stderrAccum = new DataAccumulator();

    public WriteSgTask(List<String> options, File inputFile, File outputFile, File directory) {
        this.options = options;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.directory = directory;
    }

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
        this.monitor = monitor;
        ArrayList<String> command = new ArrayList<>();

        // Name of the executable
        String toolName = ExecutableUtils.getAbsoluteCommandPath(PetrifySettings.getCommand());
        command.add(toolName);
        command.add("-write_sg");

        // Built-in arguments
        if (options != null) {
            for (String arg : options) {
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

        ExternalProcessTask task = new ExternalProcessTask(command, directory);
        Result<? extends ExternalProcessOutput> result = task.run(monitor);
        if (!result.isSuccess()) {
            return result;
        }

        ExternalProcessOutput output = result.getPayload();
        if (output.getReturnCode() == 0) {
            return Result.success(output);
        } else {
            return Result.failure(output);
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
