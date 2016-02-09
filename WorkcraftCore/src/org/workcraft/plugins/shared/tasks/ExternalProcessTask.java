package org.workcraft.plugins.shared.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.util.DataAccumulator;
import org.workcraft.util.LogUtils;

public class ExternalProcessTask implements Task<ExternalProcessResult>, ExternalProcessListener {
    private List<String> args;
    private final File workingDir;
    private boolean printStdout;
    private boolean printStderr;

    private volatile boolean finished;
    private volatile int returnCode;
    private boolean userCancelled = false;
    private ProgressMonitor<? super ExternalProcessResult> monitor;

    private DataAccumulator stdoutAccum = new DataAccumulator();
    private DataAccumulator stderrAccum = new DataAccumulator();

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
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        this.monitor = monitor;

        ExternalProcess process = new ExternalProcess(args.toArray(new String[args.size()]), workingDir);

        process.addListener(this);

        try {
            printCommandLine(this.args);
            process.start();
        } catch (IOException e) {
            return Result.exception(e);
        }

        while (true) {
            if (monitor.isCancelRequested() && process.isRunning()) {
                process.cancel();
                userCancelled = true;
            }
            if (finished)
                break;
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                process.cancel();
                userCancelled = true;
                break;
            }
        }

        if (userCancelled) {
            return Result.cancelled();
        }
        ExternalProcessResult result = new ExternalProcessResult(
                returnCode, stdoutAccum.getData(), stderrAccum.getData(),
                Collections.<String, byte[]>emptyMap());

        return Result.finished(result);
    }

    public static String getCommandLine(List<String> args) {
        String command = "";
        for (String arg: args) {
            if (command.isEmpty()) {
                command = "";
            } else {
                command += " ";
            }
            command += arg;
        }
        return command;
    }

    public static  void printCommandLine(List<String> args) {
        LogUtils.logInfoLine("Running external command: " + getCommandLine(args));
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
            printData(data, LogUtils.PREFIX_STDOUT);
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
            printData(data, LogUtils.PREFIX_STDERR);
        }
    }

    @Override
    public void processFinished(int returnCode) {
        this.returnCode = returnCode;
        this.finished = true;
    }

    private void printData(byte[] data, String prefix) {
        String text = new String(data);
        System.out.print(prefix + text);
    }

}
