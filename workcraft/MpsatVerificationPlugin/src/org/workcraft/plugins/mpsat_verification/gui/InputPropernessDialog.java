package org.workcraft.plugins.mpsat_verification.gui;

import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.gui.lists.MultipleListSelectionModel;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.InputPropernessParameters;
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

public class InputPropernessDialog extends ModalDialog<InputPropernessDataPreserver> {

    private InputSignalList inputSignalList;

    static class InputSignalList extends JList<String> {
        InputSignalList(Set<String> signals) {
            super(new Vector<>(signals.stream()
                    .sorted(SortUtils::compareNatural)
                    .collect(Collectors.toList())));

            setBorder(GuiUtils.getEmptyBorder());
            setSelectionModel(new MultipleListSelectionModel());
            setCellRenderer(new ColorListCellRenderer(signal -> SignalCommonSettings.getInputColor()));
        }
    }

    public InputPropernessDialog(Window owner, InputPropernessDataPreserver userData) {
        super(owner, "Input properness", userData);

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
        inputSignalList = new InputSignalList(stg.getSignalReferences(Signal.Type.INPUT));

        InputPropernessParameters parameters = getUserData().loadData();
        List<String> exceptionSignals = parameters.getOrderedExceptionSignals();
        selectSignals(inputSignalList, exceptionSignals);

        JButton clearButton = new JButton("Clear selection");
        clearButton.addActionListener(event -> inputSignalList.clearSelection());

        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.setBorder(GuiUtils.getEmptyBorder());

        result.add(new JLabel("Select exceptions:"), BorderLayout.NORTH);
        result.add(new JScrollPane(inputSignalList), BorderLayout.CENTER);
        result.add(clearButton, BorderLayout.SOUTH);
        return result;
    }

    private void selectSignals(InputSignalList inputSignalList, List<String> signals) {
        ListModel<String> signalListModel = inputSignalList.getModel();
        List<Integer> indices = new ArrayList<>();
        for (int index = 0; index < signalListModel.getSize(); index++) {
            String signal = signalListModel.getElementAt(index);
            if (signals.contains(signal)) {
                indices.add(index);
            }
        }
        // Convert ArrayList<Integer> to int[]
        int[] itemsToSelect = indices.stream().mapToInt(i -> i).toArray();
        inputSignalList.setSelectedIndices(itemsToSelect);
    }

    @Override
    public boolean okAction() {
        boolean result = super.okAction();
        if (result) {
            Set<String> exceptionSignals = new HashSet<>(inputSignalList.getSelectedValuesList());
            InputPropernessParameters parameters = new InputPropernessParameters(exceptionSignals);
            getUserData().saveData(parameters);
        }
        return result;
    }

}
