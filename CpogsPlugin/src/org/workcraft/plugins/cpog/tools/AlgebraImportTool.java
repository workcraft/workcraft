package org.workcraft.plugins.cpog.tools;

import java.io.File;

import javax.swing.JFileChooser;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.workspace.WorkspaceEntry;

public class AlgebraImportTool implements Tool {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        if (we.getModelEntry() == null) return false;
        if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
        return false;
    }

    @Override
    public String getSection() {
        return "! Algebra";
    }

    @Override
    public String getDisplayName() {
        return "Import CPOG expressions from file";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
        final ToolboxPanel toolbox = editor.getToolBox();
        final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);

        editor.getWorkspaceEntry().captureMemento();

        JFileChooser chooser = new JFileChooser();
        File f;
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

            f = chooser.getSelectedFile();

            if (tool.insertCpogFromFile(f)) {
                editor.getWorkspaceEntry().saveMemento();
            } else {
                editor.getWorkspaceEntry().cancelMemento();
            }

        }
    }

}
