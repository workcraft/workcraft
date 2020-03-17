package org.workcraft.plugins.shutters.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shutters.ShuttersSettings;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class LtscatResultHandler extends AbstractResultHandler<LtscatResult>  {

    private final WorkspaceEntry we;
    private final File tmpDir;

    public LtscatResultHandler(WorkspaceEntry we, File tmpDir) {
        this.we = we;
        this.tmpDir = tmpDir;
    }

    @Override
    public void handleResult(Result<? extends LtscatResult> result) {
        if (result.getOutcome() == Outcome.SUCCESS) {
            int windows = 1;

            // Print stdout of Ltscat
            System.out.println(result.getPayload().getStdout());

            // Import windows extracted
            String windowFileName = getWindowName(windows++);

            File file = new File(windowFileName);
            Framework framework = Framework.getInstance();

            while (file.exists() && !file.isDirectory()) {
                Path<String> path = we.getWorkspacePath();
                Path<String> directory = path.getParent();
                String name = FileUtils.getFileNameWithoutExtension(file);
                StgImporter importer = new StgImporter();
                InputStream inputStream = null;
                StgModel model = null;

                try {
                    inputStream = new FileInputStream(file);
                    model = importer.importStg(inputStream);
                } catch (Exception e1) {
                    FileUtils.deleteOnExitRecursively(tmpDir);
                    e1.printStackTrace();
                }

                ModelEntry me = new ModelEntry(new StgDescriptor(), model);

                framework.createWork(me, directory, name);
                windowFileName = getWindowName(windows++);
                file = new File(windowFileName);
            }

            // calling shutters
            final TaskManager taskManager = framework.getTaskManager();
            final ShuttersTask shuttersTask = new ShuttersTask(we, tmpDir);
            final ShuttersResultHandler shuttersResult = new ShuttersResultHandler(tmpDir);
            taskManager.queue(shuttersTask, "Shutters - process windows", shuttersResult);

        } else if (result.getOutcome() == Outcome.FAILURE) {
            String errorMessage = result.getPayload().getError();
            Framework framework = Framework.getInstance();
            JOptionPane.showMessageDialog(framework.getMainWindow(), errorMessage, "Ltscat error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getWindowName(int window) {
        return tmpDir.getAbsolutePath()
                + (DesktopApi.getOs().isWindows() ? "\\" : "/")
                + we.getTitle()
                + window
                + ShuttersSettings.getWindowsExtension();
    }

}
