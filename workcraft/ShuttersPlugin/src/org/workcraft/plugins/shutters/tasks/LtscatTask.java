package org.workcraft.plugins.shutters.tasks;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.shutters.ShuttersSettings;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;

public class LtscatTask implements Task<LtscatOutput>, ExternalProcessListener {

    private final WorkspaceEntry we;
    private final File tmpDir;
    private final File scriptFile;

    public LtscatTask(WorkspaceEntry we, File tmpDir, File scriptFile) {
        this.we = we;
        this.tmpDir = tmpDir;
        this.scriptFile = scriptFile;
    }

    @Override
    public Result<? extends LtscatOutput> run(ProgressMonitor<? super LtscatOutput> monitor) {
        ArrayList<String> args = new ArrayList<>();

        args.add(ShuttersSettings.getPython3Command());
        args.add(scriptFile.getAbsolutePath());

        // Running the tool through external process interface
        ExternalProcessTask task = new ExternalProcessTask(args, null);
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(mon);

        // Handling the result
        if (result.isCancel()) {
            FileUtils.deleteOnExitRecursively(tmpDir);
            we.cancelMemento();
            return Result.cancel();
        }

        String stdout = result.getPayload().getStdoutString();
        String stderr = result.getPayload().getStderrString();
        LtscatOutput ltscatOutput = new LtscatOutput(stderr, stdout);
        if (result.getPayload().getReturnCode() == 0) {
            return Result.success(ltscatOutput);
        }

        FileUtils.deleteOnExitRecursively(tmpDir);
        we.cancelMemento();
        return Result.failure(ltscatOutput);
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

}
