package org.workcraft.plugins.mpsat_verification.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.gui.lists.MultipleListSelectionModel;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.commands.OutputPersistencyVerificationCommand;
import org.workcraft.plugins.mpsat_verification.presets.OutputPersistencyDataPreserver;
import org.workcraft.plugins.mpsat_verification.presets.OutputPersistencyParameters;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.WorkspaceUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OutputPersistencyDialog extends ModalDialog<OutputPersistencyDataPreserver> {

    private LocalSignalList localSignalList;
    private ExceptionSetList exceptionSetList;

    static class LocalSignalList extends JList<String> {
        private final DefaultListModel<String> listModel;
        private final Set<String> signals;

        LocalSignalList(Set<String> outputSignals, Set<String> internalSignals) {
            super();
            this.signals = new HashSet<>();
            signals.addAll(outputSignals);
            signals.addAll(internalSignals);

            listModel = new DefaultListModel<>();
            setModel(listModel);
            listModel.addAll(SortUtils.getSortedNatural(signals));

            setBorder(GuiUtils.getEmptyBorder());
            setSelectionModel(new MultipleListSelectionModel());

            ColorListCellRenderer cellRenderer = new ColorListCellRenderer(signal ->
                    outputSignals.contains(signal) ? SignalCommonSettings.getOutputColor()
                            : internalSignals.contains(signal) ? SignalCommonSettings.getInternalColor() : null);

            setCellRenderer(cellRenderer);
        }

        boolean isValidItemSet(ItemSet itemSet) {
            return signals.containsAll(itemSet);
        }

        boolean isEmpty() {
            return listModel.isEmpty();
        }

        boolean hasSelectedItems() {
            return !getSelectedValuesList().isEmpty();
        }
    }

    static class ItemSet extends TreeSet<String> {
        ItemSet(Collection<String> items) {
            super(items);
        }

        String toComparisonString() {
            return String.join(" ", this);
        }

        @Override
        public String toString() {
            return "{" + String.join(", ", this) + "}";
        }
    }

    class ExceptionSetList extends JList<ItemSet> {
        private final DefaultListModel<ItemSet> listModel = new DefaultListModel<>();

        ExceptionSetList(Collection<ItemSet> validItemSets, Collection<ItemSet> invalidItemSets) {
            super();
            setModel(listModel);
            listModel.addAll(SortUtils.getSortedNatural(invalidItemSets, ItemSet::toComparisonString));
            listModel.addAll(SortUtils.getSortedNatural(validItemSets, ItemSet::toComparisonString));

            setSelectionModel(new MultipleListSelectionModel());
            setBorder(GuiUtils.getEmptyBorder());
            ColorListCellRenderer cellRenderer = new ColorListCellRenderer(OutputPersistencyDialog.this::getItemColorOrNullForInvalid);
            cellRenderer.setInvalidItemTooltip("Outdated exception will be ignored");
            setCellRenderer(cellRenderer);
        }

        void addItemInOrderAndSelect(Collection<String> item) {
            ItemSet itemSet = new ItemSet(item);
            int index = 0;
            Comparator<ItemSet> itemSetComparator = Comparator.comparing(ItemSet::toComparisonString);
            int count = listModel.size();
            while ((index < count) && (itemSetComparator.compare(listModel.get(index), itemSet) < 0)) {
                index++;
            }
            if ((index >= count) || (itemSetComparator.compare(listModel.get(index), itemSet) > 0)) {
                listModel.insertElementAt(itemSet, index);
            }
            ensureIndexIsVisible(index);
            setSelectedIndices(new int[]{index});
        }

        Collection<ItemSet> getItems() {
            return IntStream.range(0, listModel.size())
                    .mapToObj(listModel::get)
                    .collect(Collectors.toList());
        }

        void removeSelectedElements() {
            Arrays.stream(getSelectedIndices())
                    .boxed()
                    .sorted((i1, i2) -> Long.compare(i2, i1))
                    .forEach(listModel::remove);
        }

        boolean isEmpty() {
            return listModel.isEmpty();
        }
    }

    public OutputPersistencyDialog(Window owner, OutputPersistencyDataPreserver userData) {
        super(owner, OutputPersistencyVerificationCommand.TITLE, userData);
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

        OutputPersistencyParameters data = getUserData().loadData();
        List<ItemSet> validItemSets = new ArrayList<>();
        List<ItemSet> invalidItemSets = new ArrayList<>();
        for (TreeSet<String> itemTreeSet : data.getOrderedExceptionSignalSets()) {
            if (itemTreeSet != null) {
                ItemSet itemSet = new ItemSet(itemTreeSet);
                if (localSignalList.isValidItemSet(itemSet)) {
                    validItemSets.add(itemSet);
                } else {
                    invalidItemSets.add(itemSet);
                }
            }
        }
        exceptionSetList = new ExceptionSetList(validItemSets, invalidItemSets);

        JButton clearSignalSelectionButton = new JButton("Clear selection");
        clearSignalSelectionButton.addActionListener(event -> localSignalList.clearSelection());
        clearSignalSelectionButton.setEnabled(localSignalList.hasSelectedItems());
        localSignalList.addListSelectionListener(event ->
                clearSignalSelectionButton.setEnabled(localSignalList.hasSelectedItems()));

        JPanel localSignalsPanel = new JPanel(GuiUtils.createBorderLayout());
        localSignalsPanel.add(new JLabel("Local signals:"), BorderLayout.NORTH);
        localSignalsPanel.add(new JScrollPane(localSignalList), BorderLayout.CENTER);
        localSignalsPanel.add(clearSignalSelectionButton, BorderLayout.SOUTH);

        JButton removeSelectedExceptionSetsButton = new JButton("Remove selected sets");
        removeSelectedExceptionSetsButton.addActionListener(event -> exceptionSetList.removeSelectedElements());
        removeSelectedExceptionSetsButton.setEnabled(false);
        exceptionSetList.addListSelectionListener(event ->
                removeSelectedExceptionSetsButton.setEnabled(!exceptionSetList.getSelectedValuesList().isEmpty()));

        JPanel exceptionSetsPanel = new JPanel(GuiUtils.createBorderLayout());
        exceptionSetsPanel.add(new JLabel("Exception sets:"), BorderLayout.NORTH);
        exceptionSetsPanel.add(new JScrollPane(exceptionSetList), BorderLayout.CENTER);
        exceptionSetsPanel.add(removeSelectedExceptionSetsButton, BorderLayout.SOUTH);

        JButton addExceptionSetButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/double-right-arrow.svg"),
                "Add selected signals as an exception set");

        addExceptionSetButton.addActionListener(event -> {
            exceptionSetList.addItemInOrderAndSelect(localSignalList.getSelectedValuesList());
            localSignalList.clearSelection();
        });
        addExceptionSetButton.setEnabled(false);
        localSignalList.addListSelectionListener(event ->
                addExceptionSetButton.setEnabled(!localSignalList.getSelectedValuesList().isEmpty()));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addExceptionSetButton);
        buttonPanel.add(Box.createVerticalGlue());

        JPanel signalPanel = new JPanel(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL},
                new double[]{TableLayout.FILL}));

        signalPanel.add(localSignalsPanel, new TableLayoutConstraints(0, 0));
        signalPanel.add(buttonPanel, new TableLayoutConstraints(1, 0));
        signalPanel.add(exceptionSetsPanel, new TableLayoutConstraints(2, 0));

        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.setBorder(GuiUtils.getEmptyBorder());
        result.add(new JLabel("<html>" +
                        "Define sets of local signals that are allowed to disable each other.<br>" +
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

            OutputPersistencyParameters parameters = new OutputPersistencyParameters(exceptionSignalSets);
            getUserData().saveData(parameters);
        }
        return result;
    }

    public Color getItemColorOrNullForInvalid(Object item) {
        return isValidItem(item) ? exceptionSetList.getForeground() : null;
    }

    public boolean isValidItem(Object item) {
        return (item instanceof ItemSet itemSet) && localSignalList.isValidItemSet(itemSet);
    }

    @Override
    public boolean hasData() {
        return !localSignalList.isEmpty() || !exceptionSetList.isEmpty();
    }


}
