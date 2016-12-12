package org.workcraft.plugins.pcomp.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.pcomp.gui.PcompDialog;
import org.workcraft.plugins.pcomp.tasks.PcompResultHandler;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PcompTool implements Tool {

    public final String getSection() {
        return "Composition";
    }

    public final boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, StgModel.class);
    }

    @Override
    public String getDisplayName() {
        return "Parallel composition [PComp]";
    }

    @Override
    public final ModelEntry run(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logErrorLine("This tool only works in GUI mode.");
        } else {
            MainWindow mainWindow = framework.getMainWindow();
            PcompDialog dialog = new PcompDialog(mainWindow);
            GUI.centerAndSizeToParent(dialog, mainWindow);
            if (dialog.run()) {
                String tmpPrefix = FileUtils.getTempPrefix("pcomp");
                File tmpDirectory = FileUtils.createTempDirectory(tmpPrefix);
                ArrayList<File> inputFiles = new ArrayList<>();
                for (Path<String> path : dialog.getSourcePaths()) {
                    Workspace workspace = framework.getWorkspace();
                    File stgFile = exportStg(workspace.getOpenFile(path), tmpDirectory);
                    inputFiles.add(stgFile);
                }

                File outputFile = new File(tmpDirectory, "result.g");
                outputFile.deleteOnExit();

                PcompTask pcompTask = new PcompTask(inputFiles.toArray(new File[0]), outputFile, null,
                        dialog.getMode(), dialog.isSharedOutputsChecked(), dialog.isImprovedPcompChecked(),
                        tmpDirectory);

                PcompResultHandler pcompResult = new PcompResultHandler(dialog.showInEditor(), outputFile);
                TaskManager taskManager = framework.getTaskManager();
                taskManager.queue(pcompTask, "Running parallel composition [PComp]", pcompResult);
            }
        }
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
    }

    public File exportStg(WorkspaceEntry we, File directory) {
        StgModel model;
        ModelEntry modelEntry = we.getModelEntry();
        if (modelEntry.getMathModel() instanceof StgModel) {
            model = (StgModel) modelEntry.getMathModel();
        } else {
            throw new RuntimeException("Unexpected model class " + we.getClass().getName());
        }
        try {
            String prefix = we.getFileName() + "-";
            File file = FileUtils.createTempFile(prefix, StgUtils.ASTG_FILE_EXT, directory);
            PluginManager pluginManager = Framework.getInstance().getPluginManager();
            Export.exportToFile(model, file, Format.STG, pluginManager);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ModelValidationException e) {
            throw new RuntimeException(e);
        } catch (SerialisationException e) {
            throw new RuntimeException(e);
        }
    }

}