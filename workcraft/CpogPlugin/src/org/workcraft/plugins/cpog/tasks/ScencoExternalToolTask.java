package org.workcraft.plugins.cpog.tasks;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.tasks.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;

public class ScencoExternalToolTask implements Task<ScencoOutput>, ExternalProcessListener {

    private final WorkspaceEntry we;
    private final ScencoSolver solver;

    public ScencoExternalToolTask(WorkspaceEntry we, ScencoSolver solver) {
        this.we = we;
        this.solver = solver;
    }

    @Override
    public Result<? extends ScencoOutput> run(ProgressMonitor<? super ScencoOutput> monitor) {
        ArrayList<String> args = solver.getScencoArguments();
        String resultDirectoryPath = getResultDirectoryPath(args);

        // Error handling
        if (args.get(0).contains("ERROR")) {
            we.cancelMemento();
            return Result.failure(new ScencoOutput(args.get(1), args.get(2), resultDirectoryPath));
        }

        // Running the tool through external process interface
        ExternalProcessTask task = new ExternalProcessTask(args, null);
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(mon);

        // Handling the result
        if (result.isCancel()) {
            we.cancelMemento();
            return Result.cancel();
        }

        ScencoOutput scencoOutput = new ScencoOutput(result.getPayload().getStdoutString(), resultDirectoryPath);
        if (result.getPayload().getReturnCode() == 0) {
            return Result.success(scencoOutput);
        }

        we.cancelMemento();
        return Result.failure(scencoOutput);
    }

    private String getResultDirectoryPath(ArrayList<String> args) {
        String result = null;
        boolean found = false;
        for (String arg: args) {
            if (found) {
                result = arg;
                break;
            } else if ("-res".equals(arg)) {
                found = true;
            }
        }
        return result;
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

    public ScencoSolver getSolver() {
        return solver;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }
}
