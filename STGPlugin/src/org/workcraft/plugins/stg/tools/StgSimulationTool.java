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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.FontHelper;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.GraphEditor;
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
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgSimulationTool extends PetriNetSimulationTool {
    private static final int COLUMN_SIGNAL = 0;
    private static final int COLUMN_STATE = 1;
    private static final int COLUMN_VISIBILE = 2;
    private static final int COLUMN_COLOR = 3;

    protected HashMap<String, SignalData> signalDataMap = new HashMap<>();
    protected LinkedList<String> signals = new LinkedList<>();
    protected JTable stateTable;

    public StgSimulationTool() {
        super(true);
    }

    public enum SignalState {
        HIGH("1"),
        LOW("0"),
        UNDEFINED("?");

        private final String name;

        SignalState(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public SignalState toggle() {
            switch (this) {
            case HIGH: return LOW;
            case LOW: return HIGH;
            default: return this;
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

        public SignalData(String name, SignalTransition.Type type) {
            this.name = name;
            this.type = type;
        }

        public void copy(SignalData signalData) {
            if (signalData != null) {
                value = signalData.value;
                excited = signalData.excited;
                visible = signalData.visible;
                color = signalData.color;
            }
        }
    }

    private final class StateTable extends JTable {
        StateTable(StateTableModel model) {
            super(model);
            getTableHeader().setReorderingAllowed(false);
            setDragEnabled(true);
            setDropMode(DropMode.INSERT_ROWS);
            setTransferHandler(new StateTableRowTransferHandler(this));
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setRowHeight(FontHelper.getFontSizeInPixels(this.getFont()));
            setDefaultRenderer(SignalData.class, new SignalDataRenderer());
            setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
            setDefaultEditor(Boolean.class, new BooleanCellEditor());
            setDefaultRenderer(Color.class, new ColorCellRenderer());
            setDefaultEditor(Color.class, new ColorCellEditor());
        }

        @Override
        public void editingStopped(ChangeEvent e) {
            TableCellEditor cellEditor = getCellEditor();
            String signalName = signals.get(editingRow);
            if ((cellEditor != null) && (signalName != null)) {
                SignalData signalData = signalDataMap.get(signalName);
                Object value = cellEditor.getCellEditorValue();
                if ((signalData != null) && (value != null)) {
                    switch (editingColumn) {
                    case COLUMN_VISIBILE:
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
        public String getColumnName(int column) {
            switch (column) {
            case COLUMN_SIGNAL: return "Signal";
            case COLUMN_STATE: return "State";
            case COLUMN_VISIBILE: return "Visible";
            case COLUMN_COLOR: return "Color";
            default: return null;
            }
        }

        @Override
        public Class getColumnClass(int col) {
            switch (col) {
            case COLUMN_SIGNAL: return SignalData.class;
            case COLUMN_STATE: return SignalData.class;
            case COLUMN_VISIBILE: return Boolean.class;
            case COLUMN_COLOR: return Color.class;
            default: return null;
            }
        }

        @Override
        public int getRowCount() {
            return (signalDataMap == null) ? 0 : signalDataMap.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row < signalDataMap.size()) {
                String signalName = signals.get(row);
                SignalData signalData = signalDataMap.get(signalName);
                if (signalData != null) {
                    switch (col) {
                    case COLUMN_SIGNAL: return signalData;
                    case COLUMN_STATE: return signalData;
                    case COLUMN_VISIBILE: return signalData.visible;
                    case COLUMN_COLOR: return signalData.color;
                    default: return null;
                    }
                }
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
            case COLUMN_SIGNAL: return false;
            case COLUMN_STATE: return false;
            case COLUMN_VISIBILE: return true;
            case COLUMN_COLOR: return true;
            default: return false;
            }
        }

        public void reorder(int from, int to) {
            if ((from >= 0) && (from < signals.size()) && (to >= 0) && (to < signals.size()) && (from != to)) {
                String name = signals.remove(from);
                signals.add(to, name);
                fireTableDataChanged();
            }
        }
    }

    private final class SignalDataRenderer implements TableCellRenderer {
        final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            label.setText("");
            label.setBorder(PropertyEditorTable.BORDER_RENDER);
            label.setForeground(table.getForeground());
            label.setBackground(table.getBackground());
            label.setFont(table.getFont().deriveFont(Font.PLAIN));
            if ((net != null) && (value instanceof SignalData)) {
                SignalData st = (SignalData) value;
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

    public class StateTableRowTransferHandler extends TransferHandler {
        private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, "Integer Row Index");
        private JTable table = null;

        public StateTableRowTransferHandler(JTable table) {
            this.table = table;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            assert c == table;
            return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            boolean b = (info.getComponent() == table) && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
            table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
            return b;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            JTable target = (JTable) info.getComponent();
            JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
            int rowTo = dl.getRow();
            int max = table.getModel().getRowCount();
            if ((rowTo < 0) || (rowTo > max)) {
                rowTo = max;
            }
            target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            try {
                Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
                if (rowTo > rowFrom) {
                    rowTo--;
                }
                if ((rowFrom != -1) && (rowFrom != rowTo)) {
                    StateTableModel stateTableModel = (StateTableModel) table.getModel();
                    stateTableModel.reorder(rowFrom, rowTo);
                    target.getSelectionModel().addSelectionInterval(rowTo, rowTo);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int act) {
            if ((act == TransferHandler.MOVE) || (act == TransferHandler.NONE)) {
                table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

    }

    private final class TraceTableCellRendererImplementation implements TableCellRenderer {
        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }
        };

        boolean isActive(int row, int column) {
            if (column == 0) {
                if (!mainTrace.isEmpty() && branchTrace.isEmpty()) {
                    return row == mainTrace.getPosition();
                }
            } else {
                int absoluteBranchSize = mainTrace.getPosition() + branchTrace.size();
                int absoluteBranchPosition = mainTrace.getPosition() + branchTrace.getPosition();
                if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < absoluteBranchSize)) {
                    return row == absoluteBranchPosition;
                }
            }
            return false;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel result = null;
            label.setBorder(PropertyEditorTable.BORDER_RENDER);
            if ((net != null) && (value instanceof String)) {
                label.setText(value.toString());
                Node node = net.getNodeByReference(value.toString());
                Color color = getNodeColor(node);
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

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        super.createInterfacePanel(editor);
        stateTable = new StateTable(new StateTableModel());
        statePane.setViewportView(stateTable);
        traceTable.setDefaultRenderer(Object.class, new TraceTableCellRendererImplementation());
    }

    @Override
    public void updateState(final GraphEditor editor) {
        super.updateState(editor);
        updateSignalState();
        stateTable.tableChanged(new TableModelEvent(traceTable.getModel()));
    }

    public void updateSignalState() {
        initialiseSignalState();
        ArrayList<String> combinedTrace = new ArrayList<>();
        if (!mainTrace.isEmpty()) {
            combinedTrace.addAll(mainTrace.subList(0, mainTrace.getPosition()));
        }
        if (!branchTrace.isEmpty()) {
            combinedTrace.addAll(branchTrace.subList(0, branchTrace.getPosition()));
        }

        for (String ref : combinedTrace) {
            Node node = net.getNodeByReference(ref);
            if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalReference = ((STG) net).getSignalReference(transition);
                SignalData signalState = signalDataMap.get(signalReference);
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

        for (Node node: net.getTransitions()) {
            if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalReference = ((STG) net).getSignalReference(transition);
                SignalData signalData = signalDataMap.get(signalReference);
                if (signalData != null) {
                    signalData.excited |= net.isEnabled(transition);
                }
            }
        }
    }

    public void initialiseSignalState() {
        for (String signalName: signalDataMap.keySet()) {
            SignalData signalData = signalDataMap.get(signalName);
            signalData.value = SignalState.UNDEFINED;
            signalData.excited = false;
        }
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        initialiseStateMap();
        setStatePaneVisibility(true);
    }

    @Override
    public void generateTraceGraph(final GraphEditor editor) {
        STG stg = (STG) net;
        LinkedList<Pair<String, Color>> orderedTraceSignals = getOrderedTraceSignals(stg, mainTrace);
        StgToDtdConverter converter = new StgToDtdConverter(stg, mainTrace, orderedTraceSignals);
        VisualDtd dtd = converter.getVisualDtd();

        WorkspaceEntry we = editor.getWorkspaceEntry();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String desiredName = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new DtdDescriptor(), dtd);
        Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        workspace.add(directory, desiredName, me, true, true);
    }

    private LinkedList<Pair<String, Color>> getOrderedTraceSignals(STG stg, Trace trace) {
        LinkedList<Pair<String, Color>> result = new LinkedList<>();
        HashSet<String> traceSignals = new HashSet<>();
        for (String transitionName: trace) {
            Node node = stg.getNodeByReference(transitionName);
            if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalName = transition.getSignalName();
                traceSignals.add(signalName);
            }
        }
        for (String signalName: signals) {
            if (traceSignals.contains(signalName)) {
                SignalData signalData = signalDataMap.get(signalName);
                if ((signalData != null) && signalData.visible) {
                    result.add(new Pair<String, Color>(signalData.name, signalData.color));
                }
            }
        }
        return result;
    }

    @Override
    public String getTraceLabelByReference(String ref) {
        String result = null;
        if (ref != null) {
            String name = NamespaceHelper.getReferenceName(ref);
            String nameWithoutInstance = LabelParser.getTransitionName(name);
            if (nameWithoutInstance != null) {
                String path = NamespaceHelper.getReferencePath(ref);
                result = path + nameWithoutInstance;
            }
        }
        return result;
    }

    protected void initialiseStateMap() {
        STG stg = (STG) net;
        Set<String> signalSet = stg.getSignalReferences();
        HashMap<String, SignalData> newStateMap = new HashMap<>(signalSet.size());
        for (String signal: signalSet) {
            SignalData signalData = new SignalData(signal, stg.getSignalType(signal));
            signalData.copy(signalDataMap.get(signal));
            newStateMap.put(signal, signalData);
        }
        signalDataMap = newStateMap;

        signals.retainAll(signalSet);
        signalSet.removeAll(signals);
        signals.addAll(signalSet);
        updateSignalState();
    }

    private Color getNodeColor(Node node) {
        if (node instanceof SignalTransition) {
            SignalTransition transition = (SignalTransition) node;
            return getTypeColor(transition.getSignalType());
        }
        return Color.BLACK;
    }

    private Color getTypeColor(SignalTransition.Type type) {
        switch (type) {
        case INPUT:    return CommonSignalSettings.getInputColor();
        case OUTPUT:   return CommonSignalSettings.getOutputColor();
        case INTERNAL: return CommonSignalSettings.getInternalColor();
        default:       return CommonSignalSettings.getDummyColor();
        }
    }

    @Override
    protected void coloriseTokens(Transition t) {
        VisualTransition vt = ((VisualSTG) visualNet).getVisualTransition(t);
        if (vt == null) return;
        Color tokenColor = Color.black;
        ColorGenerator tokenColorGenerator = vt.getTokenColorGenerator();
        if (tokenColorGenerator != null) {
            // generate token colour
            tokenColor = tokenColorGenerator.updateColor();
        } else {
            // combine preset token colours
            for (Connection c: visualNet.getConnections(vt)) {
                if ((c.getSecond() == vt) && (c instanceof VisualConnection)) {
                    VisualConnection vc = (VisualConnection) c;
                    if (vc.isTokenColorPropagator()) {
                        if (vc.getFirst() instanceof VisualPlace) {
                            VisualPlace vp = (VisualPlace) c.getFirst();
                            tokenColor = Coloriser.colorise(tokenColor, vp.getTokenColor());
                        } else if (vc instanceof VisualImplicitPlaceArc) {
                            VisualImplicitPlaceArc vipa = (VisualImplicitPlaceArc) vc;
                            tokenColor = Coloriser.colorise(tokenColor, vipa.getTokenColor());
                        }
                    }
                }
            }
        }
        // propagate the colour to postset tokens
        for (Connection c: visualNet.getConnections(vt)) {
            if ((c.getFirst() == vt) && (c instanceof VisualConnection)) {
                VisualConnection vc = (VisualConnection) c;
                if (vc.isTokenColorPropagator()) {
                    if (vc.getSecond() instanceof VisualPlace) {
                        VisualPlace vp = (VisualPlace) c.getSecond();
                        vp.setTokenColor(tokenColor);
                    } else if (vc instanceof VisualImplicitPlaceArc) {
                        VisualImplicitPlaceArc vipa = (VisualImplicitPlaceArc) vc;
                        vipa.setTokenColor(tokenColor);
                    }
                }
            }
        }
    }

}
