package org.workcraft.plugins.mpsat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.stg.StgWorkspaceFilter;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class NwayDialog extends JDialog {

    private final WorkspaceEntry we;
    private boolean result;
    private WorkspaceChooser chooser;
    private Set<Path<String>> sourcePaths;

    public NwayDialog(Window owner, WorkspaceEntry we) {
        super(owner, "N-way conformation", ModalityType.DOCUMENT_MODAL);
        this.we = we;
        setContentPane(createContents());
        setMinimumSize(new Dimension(500, 300));
    }

    private JPanel createContents() {
        final JPanel content = new JPanel(new BorderLayout());
        content.setBorder(SizeHelper.getEmptyBorder());
        final Framework framework = Framework.getInstance();
        chooser = new WorkspaceChooser(framework.getWorkspace(), new StgWorkspaceFilter());
        chooser.setBorder(SizeHelper.getTitledBorder("Source STGs"));
        chooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);
        if (we != null) {
            chooser.checkNode(we.getWorkspacePath());
        }
        content.add(chooser, BorderLayout.CENTER);

        JPanel outputOptions = new JPanel();
        outputOptions.setLayout(new BoxLayout(outputOptions, BoxLayout.Y_AXIS));
        outputOptions.setBorder(SizeHelper.getTitledBorder("Outputs"));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runButton = GUI.createDialogButton("Run");
        runButton.addActionListener(event -> actionRun());

        JButton cancelButton = GUI.createDialogButton("Cancel");
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
        result = true;
        sourcePaths = chooser.getCheckedNodes();
        setVisible(false);
    }

    private void actionCancel() {
        result = false;
        setVisible(false);
    }

    public Set<Path<String>> getSourcePaths() {
        return sourcePaths;
    }

    public boolean run() {
        setVisible(true);
        return result;
    }

}
