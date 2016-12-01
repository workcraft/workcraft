package org.workcraft.plugins.stg.concepts;

import java.io.File;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsWritingTool extends ConversionTool {

    private boolean dotLayout;

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me == null ? false : me.getVisualModel() instanceof VisualStg;
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
            framework.getTaskManager().queue(task, "Translating concepts", resultHandler);
        }
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        return null; // !!!
    }

    public boolean getDotLayout() {
        return dotLayout;
    }

}
