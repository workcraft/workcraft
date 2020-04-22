package org.workcraft.plugins.plato.tasks;

import org.workcraft.plugins.plato.PlatoSettings;
import org.workcraft.tasks.*;
import org.workcraft.utils.DesktopApi;

import java.io.File;
import java.util.ArrayList;

public class PlatoSystemTask implements Task<ExternalProcessOutput> {

    @Override
    public Result<? extends ExternalProcessOutput> run(ProgressMonitor<? super ExternalProcessOutput> monitor) {

        ArrayList<String> command = new ArrayList<>();
        String executable = DesktopApi.getOs().isWindows() ? "translate\\System.exe" : "translate/System";
        command.add(PlatoSettings.getPlatoFolderLocation() + executable);

        ExternalProcessTask task = new ExternalProcessTask(command, new File("."));
        SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
        Result<? extends ExternalProcessOutput> result = task.run(mon);
        if (!result.isSuccess()) {
            return result;
        }
        deleteTranslateExecutable();
        ExternalProcessOutput output = result.getPayload();
        if (output.getReturnCode() == 0) {
            return Result.success(output);
        } else {
            return Result.failure(output);
        }
    }

    private void deleteTranslateExecutable() {
        String location = DesktopApi.getOs().isWindows() ? "translate\\System.exe" : "translate/System";
        File executable = new File(PlatoSettings.getPlatoFolderLocation() + location);
        executable.delete();
    }

}
