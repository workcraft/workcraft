package org.workcraft.plugins.stg.concepts;

import java.io.File;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsWritingTool extends ConversionTool {

    private boolean dotLayout;

    public boolean isApplicableTo(WorkspaceEntry we) {
        if (we.getModelEntry() == null) return false;
        if (we.getModelEntry().getVisualModel() instanceof VisualStg) return true;
        return false;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    public String getDisplayName() {
        return "Translate concepts...";
    }

    public void run(WorkspaceEntry we) {
        ConceptsWriterDialog dialog = new ConceptsWriterDialog();
        dialog.setVisible(true);

        if (dialog.getTranslate()) {
            File inputFile = dialog.getFile();
            dotLayout = dialog.getDotLayoutState();
            ConceptsTask task = new ConceptsTask(inputFile);
            ConceptsResultHandler resultHandler = new ConceptsResultHandler(this, FileUtils.getFileNameWithoutExtension(inputFile), we);

            Framework.getInstance().getTaskManager().queue(task, "Translating concepts", resultHandler);
        }
    }

    public boolean getDotLayout() {
        return dotLayout;
    }

}
