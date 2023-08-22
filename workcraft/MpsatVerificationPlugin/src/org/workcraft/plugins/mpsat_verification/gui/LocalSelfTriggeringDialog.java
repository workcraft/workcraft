package org.workcraft.plugins.mpsat_verification.gui;

import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.gui.lists.MultipleListSelectionModel;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.presets.LocalSelfTriggeringDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.LocalSelfTriggeringParameters;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.WorkspaceUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalSelfTriggeringDialog extends ModalDialog<LocalSelfTriggeringDataPreserver> {

    private LocalSignalList localSignalList;

    static class LocalSignalList extends JList<String> {
        LocalSignalList(Set<String> outputSignals, Set<String> internalSignals) {
            super(new Vector<>(Stream.concat(outputSignals.stream(), internalSignals.stream())
                    .sorted(SortUtils::compareNatural)
                    .collect(Collectors.toList())));

            setBorder(GuiUtils.getEmptyBorder());
            setSelectionModel(new MultipleListSelectionModel());
            setCellRenderer(new ColorListCellRenderer(signal -> {
                if (outputSignals.contains(signal)) {
                    return SignalCommonSettings.getOutputColor();
                } else if (internalSignals.contains(signal)) {
                    return SignalCommonSettings.getInternalColor();
                } else {
                    return null;
                }
            }));
        }
    }

    public LocalSelfTriggeringDialog(Window owner, LocalSelfTriggeringDataPreserver userData) {
        super(owner, "Absence of local self-triggering", userData);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                requestFocus();
            }
        });
    }

    @Override
    public JPanel createContentPanel() {
        Stg stg = WorkspaceUtils.getAs(getUserData().getWorkspaceEntry(), Stg.class);
        localSignalList = new LocalSignalList(
                stg.getSignalReferences(Signal.Type.OUTPUT),
                stg.getSignalReferences(Signal.Type.INTERNAL));

        LocalSelfTriggeringParameters parameters = getUserData().loadData();
        Set<String> exceptionSignals = parameters.getExceptionSignals();
        selectSignals(localSignalList, exceptionSignals);

        JButton clearButton = new JButton("Clear selection");
        clearButton.addActionListener(event -> localSignalList.clearSelection());

        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.setBorder(GuiUtils.getEmptyBorder());

        result.add(new JLabel("Select signals that are allowed to self-trigger:"), BorderLayout.NORTH);
        result.add(new JScrollPane(localSignalList), BorderLayout.CENTER);
        result.add(clearButton, BorderLayout.SOUTH);
        return result;
    }

    private void selectSignals(LocalSignalList localSignalList, Set<String> signals) {
        ListModel<String> signalListModel = localSignalList.getModel();
        List<Integer> indices = new ArrayList<>();
        for (int index = 0; index < signalListModel.getSize(); index++) {
            String signal = signalListModel.getElementAt(index);
            if (signals.contains(signal)) {
                indices.add(index);
            }
        }
        // Convert ArrayList<Integer> to int[]
        int[] itemsToSelect = indices.stream().mapToInt(i -> i).toArray();
        localSignalList.setSelectedIndices(itemsToSelect);
    }

    @Override
    public boolean okAction() {
        boolean result = super.okAction();
        if (result) {
            Set<String> exceptionSignals = new HashSet<>(localSignalList.getSelectedValuesList());
            LocalSelfTriggeringParameters parameters = new LocalSelfTriggeringParameters(exceptionSignals);
            getUserData().saveData(parameters);
        }
        return result;
    }

}
