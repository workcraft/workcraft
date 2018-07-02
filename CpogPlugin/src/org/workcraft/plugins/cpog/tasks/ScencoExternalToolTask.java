package org.workcraft.plugins.cpog.tasks;

import java.util.ArrayList;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoExternalToolTask implements Task<ScencoResult>, ExternalProcessListener {

    private final WorkspaceEntry we;
    private final ScencoSolver solver;

    public ScencoExternalToolTask(WorkspaceEntry we, ScencoSolver solver) {
        this.we = we;
        this.solver = solver;
    }

    @Override
    public Result<? extends ScencoResult> run(ProgressMonitor<? super ScencoResult> monitor) {
        ArrayList<String> args = solver.getScencoArguments();
        String resultDirectoryPath = getResultDirectoryPath(args);

        // Error handling
        if (args.get(0).contains("ERROR")) {
            we.cancelMemento();
            ScencoResult result = new ScencoResult(args.get(1), args.get(2), resultDirectoryPath);
            return new Result<>(Outcome.FAILURE, result);
        }

        // Running the tool through external process interface
        ExternalProcessTask task = new ExternalProcessTask(args, null);
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(mon);

        // Handling the result
        if (result.getOutcome() == Outcome.CANCEL) {
            we.cancelMemento();
            return new Result<>(Outcome.CANCEL);
        } else {
            final Outcome outcome;
            if (result.getPayload().getReturnCode() == 0) {
                outcome = Outcome.SUCCESS;
            } else {
                we.cancelMemento();
                outcome = Outcome.FAILURE;
            }
            ScencoResult finalResult = new ScencoResult(result.getPayload().getStdoutString(), resultDirectoryPath);
            return new Result<>(outcome, finalResult);
        }
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
