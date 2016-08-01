package org.workcraft.plugins.stg.tasks;

import java.io.File;
import java.util.ArrayList;


import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class ConceptsTask implements Task<ExternalProcessResult>{

    private File inputFile;

    public ConceptsTask(File inputFile) {
        this.inputFile = inputFile;
    }

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
        try {
            ArrayList<String> command = new ArrayList<>();

            command.add("./runghc");
            command.add("tools/concepts/translate/Main.hs");//TODO: Make translate file location a setting
            command.add(inputFile.getAbsolutePath());

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
