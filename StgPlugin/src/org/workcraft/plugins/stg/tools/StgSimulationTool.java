package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.gui.propertyeditor.BooleanCellEditor;
import org.workcraft.gui.propertyeditor.BooleanCellRenderer;
import org.workcraft.gui.propertyeditor.ColorCellEditor;
import org.workcraft.gui.propertyeditor.ColorCellRenderer;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.dtd.DtdDescriptor;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.converters.StgToDtdConverter;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.ModelEntry;

public class StgSimulationTool extends PetriSimulationTool {
    private static final int COLUMN_SIGNAL = 0;
    private static final int COLUMN_STATE = 1;
    private static final int COLUMN_VISIBLE = 2;
    private static final int COLUMN_COLOR = 3;

    protected HashMap<String, SignalData> signalDataMap = new HashMap<>();
    protected LinkedList<String> signals = new LinkedList<>();
    protected JTable stateTable;
    private JPanel panel;
    private HashMap<String, Boolean> initialSignalState = new HashMap<>();

    public StgSimulationTool() {
        super(true);
    }

    public enum SignalState {
        HIGH("1"),
        LOW("0"),
        UNDEFINED("?");

        private final String name;

        SignalState(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public SignalState toggle() {
            switch (this) {
                case HIGH:
                    return LOW;
                case LOW:
                    return HIGH;
                default:
                    return this;
            }
        }
    }

    public final class SignalData {
        public final String name;
        public final SignalTransition.Type type;

        public SignalState value = SignalState.UNDEFINED;
        public boolean excited = false;
        public Boolean visible = true;
        public Color color = Color.BLACK;

        public SignalData(final String name, final SignalTransition.Type type) {
            this.name = name;
            this.type = type;
        }

        public void copy(final SignalData signalData) {
            if (signalData != null) {
                value = signalData.value;
                excited = signalData.excited;
                visible = signalData.visible;
                color = signalData.color;
            }
        }
    }

    @SuppressWarnings("serial")
    private final class StateTable extends JTable {
        StateTable(final StateTableModel model) {
            super(model);
            getTableHeader().setReorderingAllowed(false);
            setDragEnabled(true);
            setDropMode(DropMode.INSERT_ROWS);
            setTransferHandler(new StateTableRowTransferHandler(this));
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setRowHeight(SizeHelper.getComponentHeightFromFont(this.getFont()));
            setDefaultRenderer(SignalData.class, new SignalDataRenderer());
            setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
            setDefaultEditor(Boolean.class, new BooleanCellEditor());
            setDefaultRenderer(Color.class, new ColorCellRenderer());
            setDefaultEditor(Color.class, new ColorCellEditor());
        }

        @Override
        public void editingStopped(final ChangeEvent e) {
            final TableCellEditor cellEditor = getCellEditor();
            final String signalName = signals.get(editingRow);
            if ((cellEditor != null) && (signalName != null)) {
                final SignalData signalData = signalDataMap.get(signalName);
                final Object value = cellEditor.getCellEditorValue();
                if ((signalData != null) && (value != null)) {
                    switch (editingColumn) {
                        case COLUMN_VISIBLE:
                            signalData.visible = (Boolean) value;
                            break;
                        case COLUMN_COLOR:
                            signalData.color = (Color) value;
                            break;
                    }
                    setValueAt(value, editingRow, editingColumn);
                    removeEditor();
                }
            }
        }
    }

    @SuppressWarnings("serial")
    private final class StateTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(final int column) {
            switch (column) {
                case COLUMN_SIGNAL:
                    return "Signal";
                case COLUMN_STATE:
                    return "State";
                case COLUMN_VISIBLE:
                    return "Visible";
                case COLUMN_COLOR:
                    return "Color";
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(final int col) {
            switch (col) {
                case COLUMN_SIGNAL:
                    return SignalData.class;
                case COLUMN_STATE:
                    return SignalData.class;
                case COLUMN_VISIBLE:
                    return Boolean.class;
                case COLUMN_COLOR:
                    return Color.class;
                default:
                    return null;
            }
        }

        @Override
        public int getRowCount() {
            return (signalDataMap == null) ? 0 : signalDataMap.size();
        }

        @Override
        public Object getValueAt(final int row, final int col) {
            if (row < signalDataMap.size()) {
                final String signalName = signals.get(row);
                final SignalData signalData = signalDataMap.get(signalName);
                if (signalData != null) {
                    switch (col) {
                        case COLUMN_SIGNAL:
                            return signalData;
                        case COLUMN_STATE:
                            return signalData;
                        case COLUMN_VISIBLE:
                            return signalData.visible;
                        case COLUMN_COLOR:
                            return signalData.color;
                        default:
                            return null;
                    }
                }
            }
            return null;
        }

        @Override
        public boolean isCellEditable(final int row, final int col) {
            switch (col) {
                case COLUMN_SIGNAL:
                    return false;
                case COLUMN_STATE:
                    return false;
                case COLUMN_VISIBLE:
                    return true;
                case COLUMN_COLOR:
                    return true;
                default:
                    return false;
            }
        }

        public void reorder(final int from, final int to) {
            if ((from >= 0) && (from < signals.size()) && (to >= 0) && (to < signals.size()) && (from != to)) {
                final String name = signals.remove(from);
                signals.add(to, name);
                fireTableDataChanged();
            }
        }
    }

    private final class SignalDataRenderer implements TableCellRenderer {
        @SuppressWarnings("serial")
        final JLabel label = new JLabel() {
            @Override
            public void paint(final Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }
        };

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                       final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            label.setText("");
            label.setBorder(PropertyEditorTable.BORDER_RENDER);
            label.setForeground(table.getForeground());
            label.setBackground(table.getBackground());
            label.setFont(table.getFont().deriveFont(Font.PLAIN));
            if (isActivated() && (value instanceof SignalData)) {
                final SignalData st = (SignalData) value;
                switch (column) {
                    case COLUMN_SIGNAL:
                        label.setText(st.name);
                        label.setForeground(getTypeColor(st.type));
                        break;
                    case COLUMN_STATE:
                        label.setText(st.value.toString());
                        if (st.excited) {
                            label.setFont(table.getFont().deriveFont(Font.BOLD));
                        }
                        break;
                    default:
                        break;
                }
            }
            return label;
        }
    }

    @SuppressWarnings("serial")
    public class StateTableRowTransferHandler extends TransferHandler {
        private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, "Integer Row Index");
        private final JTable table;

        public StateTableRowTransferHandler(final JTable table) {
            this.table = table;
        }

        @Override
        protected Transferable createTransferable(final JComponent c) {
            assert c == table;
            return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
        }

        @Override
        public boolean canImport(final TransferHandler.TransferSupport info) {
            final boolean b = (info.getComponent() == table) && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
            table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
            return b;
        }

        @Override
        public int getSourceActions(final JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(final TransferHandler.TransferSupport info) {
            final JTable target = (JTable) info.getComponent();
            final JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
            int rowTo = dl.getRow();
            final int max = table.getModel().getRowCount();
            if ((rowTo < 0) || (rowTo > max)) {
                rowTo = max;
            }
            target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            try {
                final Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
                if (rowTo > rowFrom) {
                    rowTo--;
                }
                if ((rowFrom != -1) && (rowFrom != rowTo)) {
                    final StateTableModel stateTableModel = (StateTableModel) table.getModel();
                    stateTableModel.reorder(rowFrom, rowTo);
                    target.getSelectionModel().addSelectionInterval(rowTo, rowTo);
                    return true;
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void exportDone(final JComponent c, final Transferable t, final int act) {
            if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
                table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

    }

    private final class TraceTableCellRendererImplementation implements TableCellRenderer {
        @SuppressWarnings("serial")
        private final JLabel label = new JLabel() {
            @Override
            public void paint(final Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }
        };

        boolean isActive(final int row, final int column) {
            if (column == 0) {
                if (!mainTrace.isEmpty() && branchTrace.isEmpty()) {
                    return row == mainTrace.getPosition();
                }
            } else {
                final int absoluteBranchSize = mainTrace.getPosition() + branchTrace.size();
                final int absoluteBranchPosition = mainTrace.getPosition() + branchTrace.getPosition();
                if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < absoluteBranchSize)) {
                    return row == absoluteBranchPosition;
                }
            }
            return false;
        }

        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                       final boolean isSelected, final boolean hasFocus, final int row, final int column) {
            JLabel result = null;
            label.setBorder(PropertyEditorTable.BORDER_RENDER);
            if (isActivated() && (value instanceof String)) {
                label.setText(value.toString());
                final Node node = getUnderlyingStg().getNodeByReference(value.toString());
                final Color color = getNodeColor(node);
                label.setForeground(color);
                if (isActive(row, column)) {
                    label.setBackground(table.getSelectionBackground());
                } else {
                    label.setBackground(table.getBackground());
                }
                result = label;
            }
            return result;
        }
    }

    public Stg getUnderlyingStg() {
        return (Stg) getUnderlyingModel().getMathModel();
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel == null) {
            panel = super.getControlsPanel(editor);
            stateTable = new StateTable(new StateTableModel());
            statePane.setViewportView(stateTable);
            traceTable.setDefaultRenderer(Object.class, new TraceTableCellRendererImplementation());
        }
        return panel;
    }

    @Override
    public void updateState(final GraphEditor editor) {
        super.updateState(editor);
        updateSignalState();
        stateTable.tableChanged(new TableModelEvent(stateTable.getModel()));
    }

    public void updateSignalState() {
        initialiseSignalState();
        final ArrayList<String> combinedTrace = new ArrayList<>();
        if (!mainTrace.isEmpty()) {
            combinedTrace.addAll(mainTrace.subList(0, mainTrace.getPosition()));
        }
        if (!branchTrace.isEmpty()) {
            combinedTrace.addAll(branchTrace.subList(0, branchTrace.getPosition()));
        }

        for (final String ref : combinedTrace) {
            final Node node = getUnderlyingStg().getNodeByReference(ref);
            if (node instanceof SignalTransition) {
                final SignalTransition transition = (SignalTransition) node;
                final String signalReference = getUnderlyingStg().getSignalReference(transition);
                final SignalData signalState = signalDataMap.get(signalReference);
                if (signalState != null) {
                    switch (transition.getDirection()) {
                        case MINUS:
                            signalState.value = SignalState.LOW;
                            break;
                        case PLUS:
                            signalState.value = SignalState.HIGH;
                            break;
                        case TOGGLE:
                            signalState.value = signalState.value.toggle();
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        for (final Node node : getUnderlyingStg().getTransitions()) {
            if (node instanceof SignalTransition) {
                final SignalTransition transition = (SignalTransition) node;
                final String signalReference = getUnderlyingStg().getSignalReference(transition);
                final SignalData signalData = signalDataMap.get(signalReference);
                if (signalData != null) {
                    signalData.excited |= isEnabledNode(transition);
                }
            }
        }
    }

    public void initialiseSignalState() {
        for (String signal : signalDataMap.keySet()) {
            SignalData signalData = signalDataMap.get(signal);
            Boolean signalState = (initialSignalState == null) ? null : initialSignalState.get(signal);
            if (signalState == null) {
                signalData.value = SignalState.UNDEFINED;
            } else {
                signalData.value = signalState ? SignalState.HIGH : SignalState.LOW;
            }
            signalData.excited = false;
        }
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        initialSignalState = calcInitialSignalState();
        initialiseStateMap();
        setStatePaneVisibility(true);
    }

    @Override
    public void generateTraceGraph(final GraphEditor editor) {
        final Trace trace = getCombinedTrace();
        if (trace.isEmpty()) {
            DialogUtils.showWarning("Cannot generate a timing diagram for an empty trace.");
        } else {
            final Stg stg = getUnderlyingStg();
            final LinkedList<Pair<String, Color>> visibleSignals = getVisibleSignals(stg);
            final StgToDtdConverter converter = new StgToDtdConverter(stg, trace, visibleSignals);
            final VisualDtd dtd = converter.getVisualDtd();
            final Path<String> path = editor.getWorkspaceEntry().getWorkspacePath();
            final ModelEntry me = new ModelEntry(new DtdDescriptor(), dtd);
            final Framework framework = Framework.getInstance();
            framework.createWork(me, path);
        }
    }

    private LinkedList<Pair<String, Color>> getVisibleSignals(final Stg stg) {
        final LinkedList<Pair<String, Color>> result = new LinkedList<>();
        for (final String signalRef : signals) {
            final SignalData signalData = signalDataMap.get(signalRef);
            if ((signalData != null) && signalData.visible) {
                result.add(new Pair(signalData.name, signalData.color));
            }
        }
        return result;
    }

    @Override
    public String getTraceLabelByReference(final String ref) {
        String result = ref;
        if (ref != null) {
            final String name = NamespaceHelper.getReferenceName(ref);
            final String nameWithoutInstance = LabelParser.getTransitionName(name);
            if (nameWithoutInstance != null) {
                final String path = NamespaceHelper.getReferencePath(ref);
                result = path + nameWithoutInstance;
            }
        }
        return result;
    }

    public HashMap<String, Boolean> calcInitialSignalState() {
        Stg stg = getUnderlyingStg();
        return StgUtils.getInitialState(stg);
    }

    private void initialiseStateMap() {
        final Stg stg = getUnderlyingStg();
        final HashMap<String, SignalData> newStateMap = new HashMap<>();
        final LinkedList<String> allSignals = new LinkedList<>();
        for (final Type type: Type.values()) {
            final Set<String> typedSignals = stg.getSignalReferences(type);
            allSignals.addAll(typedSignals);
            for (final String signal: typedSignals) {
                final SignalData signalData = new SignalData(signal, type);
                signalData.copy(signalDataMap.get(signal));
                signalData.visible = type != Type.INTERNAL;
                newStateMap.put(signal, signalData);
            }
        }
        signalDataMap = newStateMap;
        // Preserve "old" and append "new" items of allSignals to signals list.
        signals.retainAll(allSignals);
        allSignals.removeAll(signals);
        signals.addAll(allSignals);
        updateSignalState();
    }

    private Color getNodeColor(final Node node) {
        if (node instanceof SignalTransition) {
            final SignalTransition transition = (SignalTransition) node;
            return getTypeColor(transition.getSignalType());
        }
        return Color.BLACK;
    }

    private Color getTypeColor(final SignalTransition.Type type) {
        switch (type) {
        case INPUT:    return CommonSignalSettings.getInputColor();
        case OUTPUT:   return CommonSignalSettings.getOutputColor();
        case INTERNAL: return CommonSignalSettings.getInternalColor();
        default:       return CommonSignalSettings.getDummyColor();
        }
    }

    public void coloriseTokens(final Transition t) {
        final VisualStg visualStg = (VisualStg) getUnderlyingModel();
        final VisualTransition vt = visualStg.getVisualTransition(t);
        if (vt == null) return;
        Color tokenColor = Color.black;
        final ColorGenerator tokenColorGenerator = vt.getTokenColorGenerator();
        if (tokenColorGenerator != null) {
            // generate token colour
            tokenColor = tokenColorGenerator.updateColor();
        } else {
            // combine preset token colours
            for (final Connection c: visualStg.getConnections(vt)) {
                if ((c.getSecond() == vt) && (c instanceof VisualConnection)) {
                    final VisualConnection vc = (VisualConnection) c;
                    if (vc.isTokenColorPropagator()) {
                        if (vc.getFirst() instanceof VisualPlace) {
                            final VisualPlace vp = (VisualPlace) c.getFirst();
                            tokenColor = Coloriser.colorise(tokenColor, vp.getTokenColor());
                        } else if (vc instanceof VisualImplicitPlaceArc) {
                            final VisualImplicitPlaceArc vipa = (VisualImplicitPlaceArc) vc;
                            tokenColor = Coloriser.colorise(tokenColor, vipa.getTokenColor());
                        }
                    }
                }
            }
        }
        // propagate the colour to postset tokens
        for (final Connection c: visualStg.getConnections(vt)) {
            if ((c.getFirst() == vt) && (c instanceof VisualConnection)) {
                final VisualConnection vc = (VisualConnection) c;
                if (vc.isTokenColorPropagator()) {
                    if (vc.getSecond() instanceof VisualPlace) {
                        final VisualPlace vp = (VisualPlace) c.getSecond();
                        vp.setTokenColor(tokenColor);
                    } else if (vc instanceof VisualImplicitPlaceArc) {
                        final VisualImplicitPlaceArc vipa = (VisualImplicitPlaceArc) vc;
                        vipa.setTokenColor(tokenColor);
                    }
                }
            }
        }
    }

}
