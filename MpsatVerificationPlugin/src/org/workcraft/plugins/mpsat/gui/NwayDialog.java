package org.workcraft.plugins.mpsat.gui;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.stg.StgWorkspaceFilter;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;

@SuppressWarnings("serial")
public class NwayDialog extends JDialog {

    private WorkspaceChooser chooser;
    private Set<Path<String>> sourcePaths;
    private boolean modalResult;

    public NwayDialog(Window owner) {
        super(owner, "N-way conformation", ModalityType.DOCUMENT_MODAL);
        setContentPane(createContents());
        setMinimumSize(new Dimension(500, 300));
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel createContents() {
        final JPanel content = new JPanel(new BorderLayout());
        content.setBorder(SizeHelper.getEmptyBorder());
        Workspace workspace = Framework.getInstance().getWorkspace();
        chooser = new WorkspaceChooser(workspace, new StgWorkspaceFilter());
        chooser.setBorder(SizeHelper.getTitledBorder("Source STGs"));
        chooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);
        content.add(chooser, BorderLayout.CENTER);

        JPanel outputOptions = new JPanel();
        outputOptions.setLayout(new BoxLayout(outputOptions, BoxLayout.Y_AXIS));
        outputOptions.setBorder(SizeHelper.getTitledBorder("Outputs"));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runButton = GuiUtils.createDialogButton("Run");
        runButton.addActionListener(event -> actionRun());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> actionCancel());

        buttonsPanel.add(runButton);
        buttonsPanel.add(cancelButton);

        content.add(buttonsPanel, BorderLayout.SOUTH);

        JRootPane rootPane = getRootPane();
        rootPane.registerKeyboardAction(event -> actionRun(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        rootPane.registerKeyboardAction(event -> actionCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return content;
    }

    private void actionRun() {
        modalResult = true;
        sourcePaths = chooser.getCheckedNodes();
        setVisible(false);
    }

    private void actionCancel() {
        modalResult = false;
        setVisible(false);
    }

    public void checkAll() {
        Workspace workspace = Framework.getInstance().getWorkspace();
        for (WorkspaceEntry we : workspace.getWorks()) {
            setChecked(we, true);
        }
    }

    public void setChecked(WorkspaceEntry we, boolean value) {
        chooser.setChecked(we.getWorkspacePath(), value);
    }

    public Set<Path<String>> getSourcePaths() {
        return sourcePaths;
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
