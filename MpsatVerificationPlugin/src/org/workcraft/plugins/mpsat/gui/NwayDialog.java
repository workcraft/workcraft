package org.workcraft.plugins.mpsat.gui;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.stg.StgWorkspaceFilter;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class NwayDialog extends ModalDialog<Void> {

    private WorkspaceChooser chooser;

    public NwayDialog(Window owner) {
        super(owner, "N-way conformation", null);

        // Check all works in the workspace
        Workspace workspace = Framework.getInstance().getWorkspace();
        for (WorkspaceEntry we : workspace.getWorks()) {
            setChecked(we, true);
        }
    }

    @Override
    public JPanel createControlsPanel() {
        Workspace workspace = Framework.getInstance().getWorkspace();
        chooser = new WorkspaceChooser(workspace, new StgWorkspaceFilter());
        chooser.setBorder(SizeHelper.getTitledBorder("Source STGs"));
        chooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);

        JPanel result = super.createControlsPanel();
        result.setLayout(new BorderLayout());
        result.add(chooser, BorderLayout.CENTER);
        return result;
    }

    public void setChecked(WorkspaceEntry we, boolean value) {
        chooser.setChecked(we.getWorkspacePath(), value);
    }

    public Set<Path<String>> getSourcePaths() {
        return chooser.getCheckedNodes();
    }

}
