package org.workcraft.plugins.fst.task;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.gui.DesktopApi;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.fst.ProcessWindowsSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ShuttersTask implements Task<ShuttersResult>, ExternalProcessListener {

    private static final String MSG_ESPRESSO_NOT_PRESENT = "Espresso is not present.";
    private static final String ACCESS_SHUTTERS_ERROR = "Shutters error";

    private final WorkspaceEntry we;
    private final File tmpDir;

    public ShuttersTask(WorkspaceEntry we, File tmpDir) {
        this.we = we;
        this.tmpDir = tmpDir;
    }

    @Override
    public Result<? extends ShuttersResult> run(ProgressMonitor<? super ShuttersResult> monitor) {

        ArrayList<String> args = getArguments();

        // Error handling
        if (args.get(0).contains("ERROR")) {
            we.cancelMemento();
            ShuttersResult result = new ShuttersResult(args.get(1), args.get(2));
            return new Result<ShuttersResult>(Outcome.FAILED, result);
        }

        // Running the tool through external process interface
        ExternalProcessTask task = new ExternalProcessTask(args, null);
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessResult> result = task.run(mon);

        // Handling the result
        if (result.getOutcome() == Outcome.CANCELLED) {

            FileUtils.deleteOnExitRecursively(tmpDir);
            we.cancelMemento();
            return new Result<ShuttersResult>(Outcome.CANCELLED);

        } else {

            final Outcome outcome;
            if (result.getReturnValue().getReturnCode() == 0) {
                outcome = Outcome.FINISHED;
            } else {
                FileUtils.deleteOnExitRecursively(tmpDir);
                we.cancelMemento();
                outcome = Outcome.FAILED;
            }

            String stdout = new String(result.getReturnValue().getOutput());
            String stderr = new String(result.getReturnValue().getErrors());
            ShuttersResult finalResult = new ShuttersResult(stderr, stdout);

            return new Result<ShuttersResult>(outcome, finalResult);
        }
    }

    @Override
    public void processFinished(int returnCode) {
    }

    @Override
    public void errorData(byte[] data) {
    }

    @Override
    public void outputData(byte[] data) {
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    private ArrayList<String> getArguments() {
        ArrayList<String> args = new ArrayList<String>();
        File f = null;

        args.add(ToolUtils.getAbsoluteCommandPath(ProcessWindowsSettings.getShuttersCommand()));
        args.add(tmpDir.getAbsolutePath()
                + (DesktopApi.getOs().isWindows() ? "\\" : "/")
                + we.getTitle()
                + ProcessWindowsSettings.getMarkingsExtension());

        // Espresso related arguments
        f = new File(ToolUtils.getAbsoluteCommandPath(ProcessWindowsSettings.getEspressoCommand()));
        if (!f.exists() || f.isDirectory()) {
            FileUtils.deleteOnExitRecursively(tmpDir);
            args.add("ERROR");
            args.add(MSG_ESPRESSO_NOT_PRESENT);
            args.add(ACCESS_SHUTTERS_ERROR);
            return args;
        }
        args.add("-e");
        args.add(f.getAbsolutePath());

        // ABC related arguments
        f = new File(ToolUtils.getAbsoluteCommandPath(ProcessWindowsSettings.getAbcCommand()));
        if (f.exists() && !f.isDirectory()) {
            args.add("-a");
            args.add(f.getAbsolutePath());
        } else {
            LogUtils.logWarningLine("ABC is not installed. Visit https://bitbucket.org/alanmi/abc for more information.");
        }

        // Positive mode related argument
        if (ProcessWindowsSettings.getForcePositiveMode()) {
            args.add("-p");
        }

        return args;
    }
}
