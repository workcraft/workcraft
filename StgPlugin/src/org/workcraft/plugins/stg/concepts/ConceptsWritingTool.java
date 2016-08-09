package org.workcraft.plugins.stg.concepts;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsWritingTool implements Tool {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        if (we.getModelEntry() == null) return false;
        if (we.getModelEntry().getVisualModel() instanceof VisualStg) return true;
        return false;
    }

    @Override
    public String getSection() {
        return "! Concepts";
    }

    @Override
    public String getDisplayName() {
        return "Write concepts...";
    }

    @Override
    public void run(WorkspaceEntry we) {
        ConceptsWriterDialog dialog = new ConceptsWriterDialog();
        dialog.setVisible(true);

        if (dialog.getTranslate()) {
            File inputFile = dialog.getFile();
            ConceptsTask task = new ConceptsTask(inputFile);
            ConceptsResultHandler resultHandler = new ConceptsResultHandler(this, FileUtils.getFileNameWithoutExtension(inputFile), we);

            Framework.getInstance().getTaskManager().queue(task, "Translating concepts", resultHandler);
        }
    }

}
