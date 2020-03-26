package org.workcraft.plugins.shutters.tasks;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.shutters.ShuttersSettings;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;

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
            return new Result<>(Outcome.FAILURE, result);
        }

        // Running the tool through external process interface
        ExternalProcessTask task = new ExternalProcessTask(args, null);
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(mon);

        // Handling the result
        if (result.getOutcome() == Outcome.CANCEL) {

            FileUtils.deleteOnExitRecursively(tmpDir);
            we.cancelMemento();
            return new Result<>(Outcome.CANCEL);

        } else {

            final Outcome outcome;
            if (result.getPayload().getReturnCode() == 0) {
                outcome = Outcome.SUCCESS;
            } else {
                FileUtils.deleteOnExitRecursively(tmpDir);
                we.cancelMemento();
                outcome = Outcome.FAILURE;
            }

            String stdout = result.getPayload().getStdoutString();
            String stderr = result.getPayload().getStderrString();
            ShuttersResult finalResult = new ShuttersResult(stderr, stdout);

            return new Result<>(outcome, finalResult);
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
        ArrayList<String> args = new ArrayList<>();
        File f = null;

        args.add(ExecutableUtils.getAbsoluteCommandPath(ShuttersSettings.getShuttersCommand()));
        args.add(tmpDir.getAbsolutePath() + File.separator + we.getTitle() + ShuttersSettings.getMarkingsExtension());

        // Espresso related arguments
        f = new File(ExecutableUtils.getAbsoluteCommandPath(ShuttersSettings.getEspressoCommand()));
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
        f = new File(ExecutableUtils.getAbsoluteCommandPath(ShuttersSettings.getAbcCommand()));
        if (f.exists() && !f.isDirectory()) {
            args.add("-a");
            args.add(f.getAbsolutePath());
        } else {
            LogUtils.logWarning("ABC is not installed. Visit https://bitbucket.org/alanmi/abc for more information.");
        }

        // Positive mode related argument
        if (ShuttersSettings.getForcePositiveMode()) {
            args.add("-p");
        }

        return args;
    }
}
