package org.workcraft.plugins.pcomp.gui;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.pcomp.commands.ParallelCompositionCommand;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.stg.StgWorkspaceFilter;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ParallelCompositionDialog extends ModalDialog<Void> {

    private WorkspaceChooser chooser;
    private JCheckBox showInEditor;
    private JRadioButton leaveOutputs;
    private JRadioButton internalize;
    private JRadioButton dummify;
    private JCheckBox sharedOutputs;
    private JCheckBox saveDetail;
    private JCheckBox improvedPcomp;

    public ParallelCompositionDialog(Window owner) {
        super(owner, "Parallel composition", null);

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

        JPanel result = super.createControlsPanel();
        result.setLayout(new BorderLayout());
        result.add(chooser, BorderLayout.CENTER);
        result.add(options, BorderLayout.EAST);

        return result;
    }

    public void setChecked(WorkspaceEntry we, boolean value) {
        chooser.setChecked(we.getWorkspacePath(), value);
    }

    public Set<Path<String>> getSourcePaths() {
        return chooser.getCheckedNodes();
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

}
