package org.workcraft.plugins.shared.tasks;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.util.DataAccumulator;
import org.workcraft.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ExternalProcessTask implements Task<ExternalProcessOutput>, ExternalProcessListener {
    private List<String> args;
    private final File workingDir;
    private boolean printStdout;
    private boolean printStderr;

    private volatile boolean finished;
    private volatile int returnCode;
    private boolean userCancelled = false;
    private ProgressMonitor<? super ExternalProcessOutput> monitor;

    private final DataAccumulator stdoutAccum = new DataAccumulator();
    private final DataAccumulator stderrAccum = new DataAccumulator();

    public ExternalProcessTask(List<String> args, File workingDir) {
        this(args, workingDir, false, false);
    }

    public ExternalProcessTask(List<String> args, File workingDir, boolean printStdout, boolean printStderr) {
        this.args = args;
        this.workingDir = workingDir;
        this.printStdout = printStdout;
        this.printStderr = printStderr;
    }

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
        this.monitor = monitor;

        String workingDirectoryPath = workingDir == null ? null : workingDir.getAbsolutePath();
        ExternalProcess process = new ExternalProcess(args.toArray(new String[args.size()]), workingDirectoryPath);

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
            return Result.cancelation();
        }

        ExternalProcessOutput output = new ExternalProcessOutput(
                returnCode, stdoutAccum.getData(), stderrAccum.getData(),
                Collections.emptyMap());

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
            String text = new String(data);
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
            String text = new String(data);
            LogUtils.logStderr(text);
        }
    }

    @Override
    public void processFinished(int returnCode) {
        try {
            stdoutAccum.flush();
            stderrAccum.flush();
        } catch (IOException e) {
        }
        this.returnCode = returnCode;
        this.finished = true;
    }

}
