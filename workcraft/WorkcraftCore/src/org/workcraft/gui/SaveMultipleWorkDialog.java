package org.workcraft.gui;

import org.workcraft.Framework;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SaveMultipleWorkDialog extends ModalDialog<Void> {

    private WorkspaceChooser chooser;

    public SaveMultipleWorkDialog(Window owner) {
        super(owner, "Save modified works", null);

        Workspace workspace = Framework.getInstance().getWorkspace();
        for (WorkspaceEntry we : workspace.getWorks()) {
            chooser.setChecked(we.getWorkspacePath(), true);
        }
        // Set minimal size before reducing to screen dimension
        setMinimumSize(new Dimension(300, 300));
        GuiUtils.reduceToScreen(this, 0.5f, 0.5f);
    }

    @Override
    public JPanel createContentPanel() {
        Workspace workspace = Framework.getInstance().getWorkspace();
        chooser = new WorkspaceChooser(workspace, path -> {
            WorkspaceEntry we = workspace.getWork(path);
            return (we != null) && we.isChanged();
        });
        chooser.setBorder(GuiUtils.getTitledBorder("Select modified models to save:"));
        chooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);
        chooser.setFileterVisibility(false);

        JPanel result = super.createContentPanel();
        result.setLayout(new BorderLayout());
        result.add(chooser, BorderLayout.CENTER);
        return result;
    }

    public List<WorkspaceEntry> getCheckedWorks() {
        List<WorkspaceEntry> result = new ArrayList<>();
        Workspace workspace = Framework.getInstance().getWorkspace();

        // First, modified works that have a file (ordered by title)
        result.addAll(chooser.getCheckedNodes().stream()
                .filter(we -> workspace.getFile(we).exists())
                .map(workspace::getWork)
                .sorted((we1, we2) -> SortUtils.compareNatural(we1.getTitle(), we2.getTitle()))
                .toList());

        // Then, modified works without a file (ordered by title)
        result.addAll(chooser.getCheckedNodes().stream()
                .filter(we -> !workspace.getFile(we).exists())
                .map(workspace::getWork)
                .sorted((we1, we2) -> SortUtils.compareNatural(we1.getTitle(), we2.getTitle()))
                .toList());

        return result;
    }

}
