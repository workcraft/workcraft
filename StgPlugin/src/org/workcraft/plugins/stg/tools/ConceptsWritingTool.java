package org.workcraft.plugins.stg.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.gui.ConceptsWriterDialog;
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
    }

}
