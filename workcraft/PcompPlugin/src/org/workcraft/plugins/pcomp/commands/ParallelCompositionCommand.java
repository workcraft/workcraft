package org.workcraft.plugins.pcomp.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.pcomp.gui.ParallelCompositionDialog;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompResultHandlingMonitor;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.types.Pair;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class ParallelCompositionCommand
        implements ScriptableDataCommand<WorkspaceEntry, Pair<Collection<WorkspaceEntry>, PcompParameters>> {

    @Override
    public String getSection() {
        return "Composition";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return (we == null) || WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public String getDisplayName() {
        return "Parallel composition [PComp]";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        ParallelCompositionDialog dialog = new ParallelCompositionDialog(mainWindow);
        if (dialog.reveal()) {
            Collection<WorkspaceEntry> wes = new ArrayList<>();
            for (Path<String> path : dialog.getSourcePaths()) {
                Workspace workspace = framework.getWorkspace();
                wes.add(workspace.getWork(path));
            }
            PcompParameters parameters = dialog.getPcompParameters();
            Pair<Collection<WorkspaceEntry>, PcompParameters> data = Pair.of(wes, parameters);
            queueTask(data);
        }
    }

    private PcompResultHandlingMonitor queueTask(Pair<Collection<WorkspaceEntry>, PcompParameters> data) {
        PcompResultHandlingMonitor monitor = new PcompResultHandlingMonitor();
        Collection<WorkspaceEntry> wes = data.getFirst();
        if (wes.size() < 2) {
            monitor.isFinished(Result.exception("At least 2 STGs are required for parallel composition."));
        } else {
            Collection<Mutex> mutexes = new HashSet<>();
            File directory = FileUtils.createTempDirectory();
            ArrayList<File> inputFiles = new ArrayList<>();
            for (WorkspaceEntry inputWe : wes) {
                Stg stg = WorkspaceUtils.getAs(inputWe, Stg.class);
                mutexes.addAll(MutexUtils.getMutexes(stg));
                File inputFile = exportStg(inputWe, directory);
                inputFiles.add(inputFile);
            }
            MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
            monitor.setMutexes(mutexes);

            PcompParameters parameters = data.getSecond();
            PcompTask pcompTask = new PcompTask(inputFiles, parameters, directory);
            TaskManager taskManager = Framework.getInstance().getTaskManager();
            taskManager.queue(pcompTask, "Running parallel composition [PComp]", monitor);
        }
        return monitor;
    }

    @Override
    public Pair<Collection<WorkspaceEntry>, PcompParameters> deserialiseData(String data) {
        Collection<WorkspaceEntry> wes = PcompUtils.deserealiseData(data);
        PcompParameters parameters = new PcompParameters(PcompSettings.getSharedSignalMode(), false, false);
        return Pair.of(wes, parameters);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we, Pair<Collection<WorkspaceEntry>, PcompParameters> data) {
        return queueTask(data).waitForHandledResult();
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
            ExportUtils.exportToFile(model, file, stgFormat);
            return file;
        } catch (IOException | ModelValidationException | SerialisationException e) {
            throw new RuntimeException(e);
        }
    }

}
