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
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PcompTool implements Tool {

    public final String getSection() {
        return "Composition";
    }

    public final boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, STGModel.class);
    }

    @Override
    public String getDisplayName() {
        return "Parallel composition [PComp]";
    }

    public final void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
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
                    dialog.getMode(), dialog.isSharedOutputsChecked(), dialog.isImprovedPcompChecked(), tmpDirectory);

            PcompResultHandler pcompResult = new PcompResultHandler(dialog.showInEditor(), outputFile);
            framework.getTaskManager().queue(pcompTask, "Running parallel composition [PComp]", pcompResult);
        }
    }

    public File exportStg(WorkspaceEntry we, File directory) {
        STGModel model;
        ModelEntry modelEntry = we.getModelEntry();
        if (modelEntry.getMathModel() instanceof STGModel) {
            model = (STGModel) modelEntry.getMathModel();
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