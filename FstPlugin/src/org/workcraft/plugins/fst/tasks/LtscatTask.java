package org.workcraft.plugins.fst.tasks;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.fst.ProcessWindowsSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class LtscatTask implements Task<LtscatResult>, ExternalProcessListener {

    private final WorkspaceEntry we;
    private final File tmpDir;
    private final File scriptFile;

    public LtscatTask(WorkspaceEntry we, File tmpDir, File scriptFile) {
        this.we = we;
        this.tmpDir = tmpDir;
        this.scriptFile = scriptFile;
    }

    @Override
    public Result<? extends LtscatResult> run(ProgressMonitor<? super LtscatResult> monitor) {
        ArrayList<String> args = new ArrayList<String>();

        args.add(ProcessWindowsSettings.getPython3Command());
        args.add(scriptFile.getAbsolutePath());

        // Running the tool through external process interface
        ExternalProcessTask task = new ExternalProcessTask(args, null);
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(mon);

        // Handling the result
        if (result.getOutcome() == Outcome.CANCEL) {
            FileUtils.deleteOnExitRecursively(tmpDir);
            we.cancelMemento();
            return new Result<LtscatResult>(Outcome.CANCEL);
        } else {
            final Outcome outcome;
            if (result.getPayload().getReturnCode() == 0) {
                outcome = Outcome.SUCCESS;
            } else {
                FileUtils.deleteOnExitRecursively(tmpDir);
                we.cancelMemento();
                outcome = Outcome.FAILURE;
            }
            String stdout = new String(result.getPayload().getStdout());
            String stderr = new String(result.getPayload().getStderr());
            LtscatResult finalResult = new LtscatResult(stderr, stdout);
            return new Result<LtscatResult>(outcome, finalResult);
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
}
