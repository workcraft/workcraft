package org.workcraft.plugins.plato.commands;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.plato.gui.PlatoWriterDialog;
import org.workcraft.plugins.plato.tasks.PlatoResultHandler;
import org.workcraft.plugins.plato.tasks.PlatoTask;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PlatoStgConversionCommand extends AbstractConversionCommand {

    private boolean dotLayout;

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public String getDisplayName() {
        return "Translate concepts...";
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        PlatoWriterDialog dialog = new PlatoWriterDialog(false);
        dialog.setVisible(true);
        if (!dialog.getTranslate()) {
            return null;
        } else {
            final Framework framework = Framework.getInstance();
            final TaskManager taskManager = framework.getTaskManager();
            WorkspaceEntry we = framework.getMainWindow().getCurrentWorkspaceEntry();
            File inputFile = dialog.getFile();
            dotLayout = dialog.getDotLayoutState();
            PlatoTask task = new PlatoTask(inputFile, dialog.getIncludeList(), false);
            String name = FileUtils.getFileNameWithoutExtension(inputFile);
            PlatoResultHandler resultHandler = new PlatoResultHandler(this, name, we);
            taskManager.queue(task, "Plato - Translating concepts to STG", resultHandler);
            return null;
        }
    }

    public boolean getDotLayout() {
        return dotLayout;
    }

}
