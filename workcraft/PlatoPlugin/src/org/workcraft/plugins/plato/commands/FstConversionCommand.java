package org.workcraft.plugins.plato.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.plato.gui.WriterDialog;
import org.workcraft.plugins.plato.tasks.PlatoResultHandler;
import org.workcraft.plugins.plato.tasks.PlatoTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class FstConversionCommand extends AbstractConversionCommand {

    private boolean dotLayout;

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualFst.class);
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
        final Framework framework = Framework.getInstance();
        WriterDialog dialog = new WriterDialog(framework.getMainWindow(), true);
        if (dialog.reveal()) {
            WorkspaceEntry we = framework.getWorkspaceEntry(me);
            File inputFile = dialog.getFile();
            dotLayout = dialog.getDotLayoutState();
            PlatoTask task = new PlatoTask(inputFile, dialog.getIncludeList(), true, isSystem(dialog));
            String name = FileUtils.getFileNameWithoutExtension(inputFile);
            PlatoResultHandler resultHandler = new PlatoResultHandler(this, name, we, isSystem(dialog));
            TaskManager taskManager = framework.getTaskManager();
            taskManager.queue(task, "Plato - Translating concepts to FST", resultHandler);
        }
        return null;
    }

    public boolean getDotLayout() {
        return dotLayout;
    }

    private boolean isSystem(WriterDialog dialog) {
        return dialog.isSystem();
    }

}
