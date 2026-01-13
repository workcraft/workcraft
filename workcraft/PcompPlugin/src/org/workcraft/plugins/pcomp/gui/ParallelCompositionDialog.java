package org.workcraft.plugins.pcomp.gui;

import org.workcraft.Framework;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.stg.StgWorkspaceFilter;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ParallelCompositionDialog extends ModalDialog<Void> {

    private WorkspaceChooser stgChooser;
    private JRadioButton outputRadio;
    private JRadioButton internalRadio;
    private JRadioButton dummyRadio;
    private JCheckBox sharedOutputsCheckbox;
    private JCheckBox improvedPcompCheckbox;

    public ParallelCompositionDialog(Window owner) {
        super(owner, "Parallel composition", null);

        // Check all works in the workspace
        Workspace workspace = Framework.getInstance().getWorkspace();
        for (WorkspaceEntry we : workspace.getWorks()) {
            stgChooser.setChecked(we.getWorkspacePath(), true);
        }
        // Set minimal size before reducing to screen dimension
        setMinimumSize(new Dimension(300, 300));
        GuiUtils.reduceToScreen(this, 0.5f, 0.5f);
    }

    @Override
    public JPanel createContentPanel() {
        Workspace workspace = Framework.getInstance().getWorkspace();
        stgChooser = new WorkspaceChooser(workspace, new StgWorkspaceFilter());
        stgChooser.setBorder(GuiUtils.getTitledBorder("Source STGs"));
        stgChooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);

        JPanel signalPanel = new JPanel(GuiUtils.createNogapFlowLayout());
        signalPanel.setBorder(GuiUtils.getTitledBorder("Conversion of shared signals"));
        outputRadio = createSharedSignalRadio(PcompParameters.SharedSignalMode.OUTPUT);
        internalRadio = createSharedSignalRadio(PcompParameters.SharedSignalMode.INTERNAL);
        dummyRadio = createSharedSignalRadio(PcompParameters.SharedSignalMode.DUMMY);

        ButtonGroup outputsGroup = new ButtonGroup();
        outputsGroup.add(outputRadio);
        outputsGroup.add(dummyRadio);
        outputsGroup.add(internalRadio);

        signalPanel.add(outputRadio);
        signalPanel.add(GuiUtils.createHGap());
        signalPanel.add(internalRadio);
        signalPanel.add(GuiUtils.createHGap());
        signalPanel.add(dummyRadio);

        JPanel compositionPanel = new JPanel(GuiUtils.createNogapGridLayout(2, 1));
        sharedOutputsCheckbox = new JCheckBox("Allow the STGs to share outputs");
        improvedPcompCheckbox = new JCheckBox("Guaranteed N-way conformation");
        compositionPanel.add(sharedOutputsCheckbox);
        compositionPanel.add(improvedPcompCheckbox);

        JPanel optionsPanel = new JPanel(GuiUtils.createBorderLayout());
        optionsPanel.add(signalPanel, BorderLayout.NORTH);
        optionsPanel.add(compositionPanel, BorderLayout.SOUTH);

        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.add(stgChooser, BorderLayout.CENTER);
        result.add(optionsPanel, BorderLayout.SOUTH);

        return result;
    }

    private JRadioButton createSharedSignalRadio(PcompParameters.SharedSignalMode mode) {
        JRadioButton result = new JRadioButton(mode.toString());
        result.setSelected(mode == PcompSettings.getSharedSignalMode());
        return result;
    }

    public Set<Path<String>> getSourcePaths() {
        return stgChooser.getCheckedNodes();
    }

    public PcompParameters getPcompParameters() {
        return new PcompParameters(getMode(), sharedOutputsCheckbox.isSelected(), improvedPcompCheckbox.isSelected());
    }

    private PcompParameters.SharedSignalMode getMode() {
        if (outputRadio.isSelected()) {
            return PcompParameters.SharedSignalMode.OUTPUT;
        }
        if (internalRadio.isSelected()) {
            return PcompParameters.SharedSignalMode.INTERNAL;
        }
        if (dummyRadio.isSelected()) {
            return PcompParameters.SharedSignalMode.DUMMY;
        }
        throw new NotSupportedException("No conversion mode for shared signals is selected.");
    }

}