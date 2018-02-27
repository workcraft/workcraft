package org.workcraft.plugins.cpog.tasks;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.ToolUtils;

public class PGMinerTask implements Task<ExternalProcessOutput> {

    private final File inputFile;
    private final boolean split;

    public PGMinerTask(File inputFile, boolean split) {
        this.inputFile = inputFile;
        this.split = split;
    }

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {
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
            Result<? extends ExternalProcessOutput> result = task.run(mon);

            if (result.getOutcome() != Outcome.SUCCESS) {
                return result;
            }

            ExternalProcessOutput output = result.getPayload();
            if (output.getReturnCode() == 0) {
                return Result.success(output);
            } else {
                return Result.failure(output);
            }
        } catch (NullPointerException e) {
            //Open window dialog was cancelled, do nothing
        }

        return null;
    }


}
