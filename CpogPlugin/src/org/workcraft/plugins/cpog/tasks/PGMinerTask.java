package org.workcraft.plugins.cpog.tasks;

import java.io.File;
import java.util.ArrayList;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.ToolUtils;

public class PGMinerTask implements Task<ExternalProcessResult> {

    private File inputFile;
    private boolean split;

    public PGMinerTask(File inputFile, boolean split) {
        this.inputFile = inputFile;
        this.split = split;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        //Build the commands for PGMiner
        try {
            ArrayList<String> command = new ArrayList<>();
            String toolName = ToolUtils.getAbsoluteCommandPath(CpogSettings.getPgminerCommand());
            command.add(toolName);

            command.add(inputFile.getAbsolutePath());

            if (split) {
                command.add("-s");
            }

            //Call PGMiner
            ExternalProcessTask task = new ExternalProcessTask(command, new File("."));
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExternalProcessResult> result = task.run(mon);

            if (result.getOutcome() != Outcome.FINISHED) {
                return result;
            }

            ExternalProcessResult retVal = result.getReturnValue();
            ExternalProcessResult finalResult = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(), retVal.getErrors(), null);
            if (retVal.getReturnCode() == 0) {
                return Result.finished(finalResult);
            } else {
                return Result.failed(finalResult);
            }
        } catch (NullPointerException e) {
            //Open window dialog was cancelled, do nothing
        }

        return null;
    }


}
