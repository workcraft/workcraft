package org.workcraft.plugins.pcomp.commands;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.pcomp.gui.ParallelCompositionDialog;
import org.workcraft.plugins.pcomp.tasks.PcompResultHandler;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.*;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ParallelCompositionCommand implements Command {

    public static final String RESULT_FILE_NAME = "result.g";
    public static final String DETAIL_FILE_NAME = "detail.xml";

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
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logError("Command '" + getClass().getSimpleName() + "' only works in GUI mode.");
            return;
        }
        MainWindow mainWindow = framework.getMainWindow();
        ParallelCompositionDialog dialog = new ParallelCompositionDialog(mainWindow);
        if (!dialog.reveal()) {
            return;
        }

        Set<Path<String>> paths = dialog.getSourcePaths();
        if ((paths == null) || (paths.size() < 2)) {
            DialogUtils.showWarning("At least 2 STGs are required for parallel composition.");
            return;
        }

        Collection<Mutex> mutexes = new HashSet<>();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        ArrayList<File> inputFiles = new ArrayList<>();
        for (Path<String> path: paths) {
            Workspace workspace = framework.getWorkspace();
            WorkspaceEntry inputWe = workspace.getWork(path);
            Stg stg = WorkspaceUtils.getAs(inputWe, Stg.class);
            Collection<Mutex> inputMutexes = MutexUtils.getMutexes(stg);
            if (inputMutexes != null) {
                mutexes.addAll(inputMutexes);
            }
            File stgFile = exportStg(inputWe, directory);
            inputFiles.add(stgFile);
        }

        File outputFile = new File(directory, RESULT_FILE_NAME);
        outputFile.deleteOnExit();
        File detailFile = null;
        if (dialog.isSaveDetailChecked()) {
            detailFile = new File(directory, DETAIL_FILE_NAME);
        }

        PcompTask pcompTask = new PcompTask(inputFiles.toArray(new File[0]), outputFile, detailFile,
                dialog.getMode(), dialog.isSharedOutputsChecked(), dialog.isImprovedPcompChecked(),
                directory);

        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        PcompResultHandler pcompResult = new PcompResultHandler(dialog.showInEditor(), outputFile, mutexes);
        TaskManager taskManager = framework.getTaskManager();
        taskManager.queue(pcompTask, "Running parallel composition [PComp]", pcompResult);
    }

    public File exportStg(WorkspaceEntry we, File directory) {
        StgModel model = WorkspaceUtils.getAs(we, StgModel.class);
        if (model == null) {
            String modelClassName = we.getModelEntry().getMathModel().getClass().getName();
            throw new RuntimeException("Unexpected model class " + modelClassName);
        }
        try {
            String prefix = we.getFileName() + "-";
            StgFormat stgFormat = StgFormat.getInstance();
            String stgFileExtension = stgFormat.getExtension();
            File file = FileUtils.createTempFile(prefix, stgFileExtension, directory);
            PluginManager pluginManager = Framework.getInstance().getPluginManager();
            ExportUtils.exportToFile(model, file, stgFormat, pluginManager);
            return file;
        } catch (IOException | ModelValidationException | SerialisationException e) {
            throw new RuntimeException(e);
        }
    }

}