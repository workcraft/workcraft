package org.workcraft.plugins.pcomp.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.pcomp.gui.PcompDialog;
import org.workcraft.plugins.pcomp.tasks.PcompResultHandler;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ParallelCompositionCommand implements Command {

    public String getSection() {
        return "Composition";
    }

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public String getDisplayName() {
        return "Parallel composition [PComp]";
    }

    @Override
    public final void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logErrorLine("Tool '" + getClass().getSimpleName() + "' only works in GUI mode.");
        } else {
            MainWindow mainWindow = framework.getMainWindow();
            PcompDialog dialog = new PcompDialog(mainWindow);
            GUI.centerToParent(dialog, mainWindow);
            Collection<Mutex> mutexes = new HashSet<>();
            if (dialog.run()) {
                String tmpPrefix = FileUtils.getTempPrefix(we.getTitle());
                File tmpDirectory = FileUtils.createTempDirectory(tmpPrefix);
                ArrayList<File> inputFiles = new ArrayList<>();
                for (Path<String> path : dialog.getSourcePaths()) {
                    Workspace workspace = framework.getWorkspace();
                    WorkspaceEntry inputWe = workspace.getWork(path);
                    Collection<Mutex> inputMutexes = MutexUtils.getMutexes(WorkspaceUtils.getAs(inputWe, Stg.class));
                    if (inputMutexes != null) {
                        mutexes.addAll(inputMutexes);
                    }
                    File stgFile = exportStg(inputWe, tmpDirectory);
                    inputFiles.add(stgFile);
                }

                File outputFile = new File(tmpDirectory, "result.g");
                outputFile.deleteOnExit();

                PcompTask pcompTask = new PcompTask(inputFiles.toArray(new File[0]), outputFile, null,
                        dialog.getMode(), dialog.isSharedOutputsChecked(), dialog.isImprovedPcompChecked(),
                        tmpDirectory);

                PcompResultHandler pcompResult = new PcompResultHandler(dialog.showInEditor(), outputFile, mutexes);
                TaskManager taskManager = framework.getTaskManager();
                taskManager.queue(pcompTask, "Running parallel composition [PComp]", pcompResult);
            }
        }
    }

    public File exportStg(WorkspaceEntry we, File directory) {
        StgModel model = WorkspaceUtils.getAs(we, StgModel.class);
        if (model == null) {
            String modelClassName = we.getModelEntry().getMathModel().getClass().getName();
            throw new RuntimeException("Unexpected model class " + modelClassName);
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