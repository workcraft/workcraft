package org.workcraft.plugins.plato.tasks;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.plato.PlatoSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class PlatoSystemTask implements Task<ExternalProcessResult> {

    @Override
    public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {

        ArrayList<String> command = new ArrayList<>();
        String executable = DesktopApi.getOs().isWindows() ? "translate\\System.exe" : "translate/System";
        command.add(PlatoSettings.getPlatoFolderLocation() + executable);

        ExternalProcessTask task = new ExternalProcessTask(command, new File("."));
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessResult> result = task.run(mon);
        if (result.getOutcome() != Outcome.SUCCESS) {
            return result;
        }
        ExternalProcessResult retVal = result.getReturnValue();
        ExternalProcessResult finalResult = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(),
                retVal.getErrors(), null);
        deleteTranslateExecutable();
        if (retVal.getReturnCode() == 0) {
            return Result.success(finalResult);
        } else {
            return Result.failure(finalResult);
        }
    }

    private void deleteTranslateExecutable() {
        String location = DesktopApi.getOs().isWindows() ? "translate\\System.exe" : "translate/System";
        File executable = new File(PlatoSettings.getPlatoFolderLocation() + location);
        executable.delete();
    }

}
