package org.workcraft.plugins.stg.concepts;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class TranslateConceptConversionCommand extends AbstractConversionCommand {

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
    public void run(WorkspaceEntry we) {
        ConceptsWriterDialog dialog = new ConceptsWriterDialog();
        dialog.setVisible(true);
        if (dialog.getTranslate()) {
            File inputFile = dialog.getFile();
            dotLayout = dialog.getDotLayoutState();
            ConceptsTask task = new ConceptsTask(inputFile);
            String name = FileUtils.getFileNameWithoutExtension(inputFile);
            ConceptsResultHandler resultHandler = new ConceptsResultHandler(this, name, we);

            Framework framework = Framework.getInstance();
            TaskManager taskManager = framework.getTaskManager();
            taskManager.queue(task, "Translating concepts", resultHandler);
        }
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

    public boolean getDotLayout() {
        return dotLayout;
    }

}
