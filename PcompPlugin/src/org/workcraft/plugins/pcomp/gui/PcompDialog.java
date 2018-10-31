package org.workcraft.plugins.pcomp.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.pcomp.commands.ParallelCompositionCommand;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.stg.StgWorkspaceFilter;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class PcompDialog extends JDialog {

    private final WorkspaceEntry we;
    private boolean result;
    private WorkspaceChooser chooser;
    private Set<Path<String>> sourcePaths;
    private JCheckBox showInEditor;
    private JRadioButton leaveOutputs;
    private JRadioButton internalize;
    private JRadioButton dummify;
    private JCheckBox sharedOutputs;
    private JCheckBox saveDetail;
    private JCheckBox improvedPcomp;

    public PcompDialog(Window owner, WorkspaceEntry we) {
        super(owner, "Parallel composition", ModalityType.DOCUMENT_MODAL);
        this.we = we;
        setContentPane(createContents());
        setMinimumSize(new Dimension(600, 400));
        pack();
    }

    private JPanel createContents() {
        double[][] sizes = {
            {TableLayout.FILL, TableLayout.PREFERRED},
            {TableLayout.FILL, TableLayout.PREFERRED},
        };

        final JPanel content = new JPanel(new TableLayout(sizes));
        content.setBorder(SizeHelper.getEmptyBorder());
        final Framework framework = Framework.getInstance();
        chooser = new WorkspaceChooser(framework.getWorkspace(), new StgWorkspaceFilter());
        chooser.setBorder(SizeHelper.getTitledBorder("Source STGs"));
        chooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);
        if (we != null) {
            chooser.checkNode(we.getWorkspacePath());
        }
        content.add(chooser, "0 0 0 1");

        showInEditor = new JCheckBox();
        showInEditor.setText("Show result in editor");
        showInEditor.setSelected(true);

        JPanel outputOptions = new JPanel();
        outputOptions.setLayout(new BoxLayout(outputOptions, BoxLayout.Y_AXIS));
        outputOptions.setBorder(SizeHelper.getTitledBorder("Shared signals"));

        leaveOutputs = new JRadioButton("Leave as outputs");
        internalize = new JRadioButton("Make internal");
        dummify = new JRadioButton("Make dummy");
        leaveOutputs.setSelected(true);

        ButtonGroup outputsGroup = new ButtonGroup();
        outputsGroup.add(leaveOutputs);
        outputsGroup.add(dummify);
        outputsGroup.add(internalize);

        outputOptions.add(leaveOutputs);
        outputOptions.add(internalize);
        outputOptions.add(dummify);

        sharedOutputs = new JCheckBox("Allow the STGs to share outputs");
        saveDetail = new JCheckBox("Save the composition details in " + ParallelCompositionCommand.DETAIL_FILE_NAME);
        improvedPcomp = new JCheckBox("Guaranteed N-way conformation");

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBorder(SizeHelper.getTitledBorder("Options"));
        options.add(showInEditor, 0);
        options.add(outputOptions, 1);
        options.add(sharedOutputs, 2);
        options.add(saveDetail, 3);
        options.add(improvedPcomp, 4);

        content.add(options, "1 0");
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runButton = GUI.createDialogButton("Run");
        runButton.addActionListener(event -> actionRun());

        JButton cancelButton = GUI.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> actionCancel());

        buttonsPanel.add(runButton);
        buttonsPanel.add(cancelButton);

        content.add(buttonsPanel, "1 1");

        getRootPane().registerKeyboardAction(event -> actionRun(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> actionCancel(),
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

    public boolean showInEditor() {
        return showInEditor.isSelected();
    }

    public boolean isSharedOutputsChecked() {
        return sharedOutputs.isSelected();
    }

    public boolean isSaveDetailChecked() {
        return saveDetail.isSelected();
    }

    public boolean isImprovedPcompChecked() {
        return improvedPcomp.isSelected();
    }

    public ConversionMode getMode() {
        if (leaveOutputs.isSelected()) {
            return ConversionMode.OUTPUT;
        }
        if (internalize.isSelected()) {
            return ConversionMode.INTERNAL;
        }
        if (dummify.isSelected()) {
            return ConversionMode.DUMMY;
        }
        throw new NotSupportedException("No button is selected. Cannot proceed.");
    }

    public boolean run() {
        setVisible(true);
        return result;
    }

}
