package org.workcraft.plugins.mpsat_verification.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.gui.lists.MultipleListSelectionModel;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.DiInterfaceParameters;
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
import java.util.stream.IntStream;

public class DiInterfaceDialog extends ModalDialog<DiInterfaceDataPreserver> {

    private InputSignalList inputSignalList;
    private ExceptionSetList exceptionSetList;

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

    static class ItemSet extends HashSet<String> {
        ItemSet(Collection<String> items) {
            super(items);
        }

        @Override
        public String toString() {
            return "{" + String.join(", ", this) + "}";
        }
    }

    static class ExceptionSetList extends JList<ItemSet> {

        private final DefaultListModel<ItemSet> model = new DefaultListModel<>();

        ExceptionSetList(Collection<ItemSet> exceptionSets) {
            super();
            setModel(model);
            model.addAll(exceptionSets);
            setBorder(GuiUtils.getEmptyBorder());
            setSelectionModel(new MultipleListSelectionModel());
            setCellRenderer(new ColorListCellRenderer(signal -> null));
        }

        public void clear() {
            model.clear();
        }

        public void addItemAndSelect(Collection<String> item) {
            model.add(model.size(), new ItemSet(item));
            setSelectedIndex(model.size());
        }

        public Collection<ItemSet> getItems() {
            return IntStream.range(0, model.size())
                    .mapToObj(model::get)
                    .collect(Collectors.toList());
        }

        public void removeSelectedElements() {
            Arrays.stream(getSelectedIndices())
                    .boxed()
                    .sorted((i1, i2) -> Long.compare(i2, i1))
                    .forEach(model::remove);
        }

    }

    public DiInterfaceDialog(Window owner, DiInterfaceDataPreserver userData) {
        super(owner, "Delay insensitive interface", userData);

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

        DiInterfaceParameters parameters = getUserData().loadData();
        List<ItemSet> exceptionSets = parameters.getExceptionSignalSets().stream()
                .map(ItemSet::new)
                .collect(Collectors.toList());

        exceptionSetList = new ExceptionSetList(exceptionSets);

        JButton clearInputSignalSelectionButton = new JButton("Clear selection");
        clearInputSignalSelectionButton.addActionListener(event -> inputSignalList.clearSelection());
        clearInputSignalSelectionButton.setEnabled(false);
        inputSignalList.addListSelectionListener(event ->
                clearInputSignalSelectionButton.setEnabled(!inputSignalList.getSelectedValuesList().isEmpty()));

        JPanel inputSignalsPanel = new JPanel(GuiUtils.createBorderLayout());
        inputSignalsPanel.add(new JLabel("Input signals:"), BorderLayout.NORTH);
        inputSignalsPanel.add(new JScrollPane(inputSignalList), BorderLayout.CENTER);
        inputSignalsPanel.add(clearInputSignalSelectionButton, BorderLayout.SOUTH);

        JButton removeSelectedExceptionSetsButton = new JButton("Remove selected sets");
        removeSelectedExceptionSetsButton.addActionListener(event -> exceptionSetList.removeSelectedElements());
        removeSelectedExceptionSetsButton.setEnabled(false);
        exceptionSetList.addListSelectionListener(event ->
                removeSelectedExceptionSetsButton.setEnabled(!exceptionSetList.getSelectedValuesList().isEmpty()));

        JPanel exceptionSetsPanel = new JPanel(GuiUtils.createBorderLayout());
        exceptionSetsPanel.add(new JLabel("Exception sets:"), BorderLayout.NORTH);
        exceptionSetsPanel.add(new JScrollPane(exceptionSetList), BorderLayout.CENTER);
        exceptionSetsPanel.add(removeSelectedExceptionSetsButton, BorderLayout.SOUTH);

        JButton addExceptionSetButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/double-right-arrow.svg"),
                "Add selected inputs as an exception set");

        addExceptionSetButton.addActionListener(event -> {
            exceptionSetList.addItemAndSelect(inputSignalList.getSelectedValuesList());
            inputSignalList.clearSelection();
            exceptionSetList.clearSelection();
        });
        addExceptionSetButton.setEnabled(false);
        inputSignalList.addListSelectionListener(event ->
                addExceptionSetButton.setEnabled(!inputSignalList.getSelectedValuesList().isEmpty()));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addExceptionSetButton);
        buttonPanel.add(Box.createVerticalGlue());

        JPanel signalPanel = new JPanel(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL},
                new double[]{TableLayout.FILL}));

        signalPanel.add(inputSignalsPanel, new TableLayoutConstraints(0, 0));
        signalPanel.add(buttonPanel, new TableLayoutConstraints(1, 0));
        signalPanel.add(exceptionSetsPanel, new TableLayoutConstraints(2, 0));

        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.setBorder(GuiUtils.getEmptyBorder());
        result.add(new JLabel("<html>" +
                        "Define sets of input signals to be excluded from delay insensitivity verification.<br>" +
                        "Note: self-triggering input is considered a violation; use singleton set to waive.<br>" +
                        "</html>"),
                BorderLayout.NORTH);

        result.add(signalPanel, BorderLayout.CENTER);
        return result;
    }

    @Override
    public boolean okAction() {
        boolean result = super.okAction();
        if (result) {
            List<Set<String>> exceptionSignalSets = exceptionSetList.getItems().stream()
                    .filter(exceptionSet -> !exceptionSet.isEmpty())
                    .collect(Collectors.toList());

            DiInterfaceParameters parameters = new DiInterfaceParameters(exceptionSignalSets);
            getUserData().saveData(parameters);
        }
        return result;
    }

}
