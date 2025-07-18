package org.workcraft.tasks;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.shared.DataAccumulator;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExternalProcessTask implements Task<ExternalProcessOutput>, ExternalProcessListener {

    private final List<String> args;
    private final File directory;
    private final boolean printStdout;
    private final boolean printStderr;

    private volatile boolean finished;
    private volatile int returnCode;
    private boolean userCancelled = false;
    private ProgressMonitor<? super ExternalProcessOutput> monitor;

    private final DataAccumulator stdoutAccum = new DataAccumulator();
    private final DataAccumulator stderrAccum = new DataAccumulator();

    public ExternalProcessTask(List<String> args, File directory) {
        this(args, directory, false, false);
    }

    public ExternalProcessTask(List<String> args, File directory, boolean printStdout, boolean printStderr) {
        this.args = args;
        this.directory = directory;
        this.printStdout = printStdout;
        this.printStderr = printStderr;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
        this.monitor = monitor;

        ExternalProcess process = new ExternalProcess(args.toArray(new String[0]), directory);

        process.addListener(this);

        try {
            ExternalProcess.printCommandLine(this.args);
            process.start();
        } catch (IOException e) {
            LogUtils.logError(e.getMessage());
            return Result.exception(e);
        }

        while (true) {
            if (monitor.isCancelRequested() && process.isRunning()) {
                process.cancel();
                userCancelled = true;
            }
            if (finished) {
                break;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                process.cancel();
                userCancelled = true;
                break;
            }
        }

        if (userCancelled) {
            return Result.cancel();
        }

        ExternalProcessOutput output = new ExternalProcessOutput(
                returnCode, stdoutAccum.getData(), stderrAccum.getData());

        return Result.success(output);
    }

    @Override
    public void outputData(byte[] data) {
        try {
            stdoutAccum.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        monitor.stdout(data);
        if (printStdout) {
            String text = new String(data, StandardCharsets.UTF_8);
            LogUtils.logStdout(text);
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
        if (printStderr) {
            String text = new String(data, StandardCharsets.UTF_8);
            LogUtils.logStderr(text);
        }
    }

    @Override
    public void processFinished(int returnCode) {
        try {
            stdoutAccum.flush();
            stderrAccum.flush();
        } catch (IOException ignored) {
        }
        this.returnCode = returnCode;
        this.finished = true;
    }

}
