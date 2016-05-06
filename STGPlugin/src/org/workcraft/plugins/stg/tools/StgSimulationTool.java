package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.geom.Point2D;
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
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.FontHelper;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.dtd.Dtd;
import org.workcraft.plugins.dtd.DtdDescriptor;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualDtd.SignalEvent;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.ColorGenerator;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgSimulationTool extends PetriNetSimulationTool {
    private static final int COLUMN_SIGNAL = 0;
    private static final int COLUMN_STATE = 1;
    private static final int COLUMN_VISIBILITY = 2;
    private static final int COLUMN_COLOR = 3;

    protected HashMap<String, SignalState> stateMap = new HashMap<>();
    protected LinkedList<String> signals = new LinkedList<>();
    protected JTable stateTable;

    public StgSimulationTool() {
        super(true);
    }

    public final class SignalState {
        public final String signal;
        public final SignalTransition.Type type;

        public Color color = Color.BLACK;
        public boolean excited = false;
        public int value = -1;

        public SignalState(String signal, SignalTransition.Type type) {
            this.signal = signal;
            this.type = type;
        }

        public void copy(SignalState signalState) {
            if (signalState != null) {
                color = signalState.color;
                excited = signalState.excited;
                value = signalState.value;
            }
        }
    }

    @SuppressWarnings("serial")
    private final class StateTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case COLUMN_SIGNAL: return "Signal";
            case COLUMN_STATE: return "State";
            case COLUMN_VISIBILITY: return "Visibility";
            case COLUMN_COLOR: return "Color";
            default: return null;
            }
        }

        @Override
        public int getRowCount() {
            return (stateMap == null) ? 0 : stateMap.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            Object result = null;
            if (row < stateMap.size()) {
                String name = signals.get(row);
                result = stateMap.get(name);
            }
            return result;
        }

        public void reorder(int from, int to) {
            if ((from >= 0) && (from < signals.size()) && (to >= 0) && (to < signals.size()) && (from != to)) {
                String name = signals.remove(from);
                signals.add(to, name);
                fireTableDataChanged();
            }
        }
    }

    private final class StateTableCellRendererImplementation implements TableCellRenderer {
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
            JLabel result = null;
            label.setBorder(PropertyEditorTable.BORDER_RENDER);
            label.setBackground(table.getBackground());
            if ((net != null) && (value instanceof SignalState)) {
                SignalState st = (SignalState) value;
                switch (column) {
                case COLUMN_SIGNAL:
                    label.setText(st.signal);
                    Color color = getTypeColor(st.type);
                    label.setForeground(color);
                    Font plainFont = table.getFont().deriveFont(Font.PLAIN);
                    label.setFont(plainFont);
                    result = label;
                    break;
                case COLUMN_STATE:
                    if (st.value < 0) {
                        label.setText("?");
                    } else {
                        label.setText(Integer.toString(st.value));
                    }
                    label.setForeground(table.getForeground());
                    if (st.excited) {
                        Font boldFont = table.getFont().deriveFont(Font.BOLD);
                        label.setFont(boldFont);
                    }
                    result = label;
                    break;
                default:
                    break;
                }
            }
            return result;
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
        stateTable = new JTable(new StateTableModel());
        stateTable.getTableHeader().setReorderingAllowed(false);
        stateTable.setDragEnabled(true);
        stateTable.setDropMode(DropMode.INSERT_ROWS);
        stateTable.setTransferHandler(new StateTableRowTransferHandler(stateTable));
        stateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stateTable.setRowHeight(FontHelper.getFontSizeInPixels(stateTable.getFont()));
        stateTable.setDefaultRenderer(Object.class, new StateTableCellRendererImplementation());
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
                SignalState signalState = stateMap.get(signalReference);
                if (signalState != null) {
                    switch (transition.getDirection()) {
                    case MINUS:
                        signalState.value = 0;
                        break;
                    case PLUS:
                        signalState.value = 1;
                        break;
                    case TOGGLE:
                        if (signalState.value == 1) {
                            signalState.value = 0;
                        } else if (signalState.value == 0) {
                            signalState.value = 1;
                        }
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
                SignalState st = stateMap.get(signalReference);
                if (st != null) {
                    st.excited |= net.isEnabled(transition);
                }
            }
        }
    }

    public void initialiseSignalState() {
        for (String signalName: stateMap.keySet()) {
            SignalState signalState = stateMap.get(signalName);
            signalState.value = -1;
            signalState.excited = false;
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
        VisualDtd dtd = generateDtd((STG) net, mainTrace);
        WorkspaceEntry we = editor.getWorkspaceEntry();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String desiredName = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new DtdDescriptor(), dtd);
        Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        workspace.add(directory, desiredName, me, true, true);
    }

    private Type convertSignalType(SignalTransition.Type type) {
        switch (type) {
        case INPUT:    return Type.INPUT;
        case OUTPUT:   return Type.OUTPUT;
        case INTERNAL: return Type.INTERNAL;
        default:       return null;
        }
    }

    private Direction getDirection(SignalTransition.Direction direction) {
        switch (direction) {
        case PLUS:  return Direction.PLUS;
        case MINUS: return Direction.MINUS;
        default:    return null;
        }
    }

    private VisualDtd generateDtd(STG stg, Trace trace) {
        VisualDtd dtd = new VisualDtd(new Dtd());
        HashMap<String, VisualSignal> signalMap = new HashMap<>();
        HashMap<Node, HashSet<SignalEvent>> causeMap = new HashMap<>();
        HashSet<String> presentSignals = new HashSet<>();
        for (String transitionName: trace) {
            Node node = stg.getNodeByReference(transitionName);
            if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalName = transition.getSignalName();
                presentSignals.add(signalName);
            }
        }
        for (String signalName: signals) {
            if (!presentSignals.contains(signalName) || signalMap.containsKey(signalName)) continue;
            VisualSignal signal = dtd.createVisualSignal(signalName);
            signal.setPosition(new Point2D.Double(0.0, 2.0 * signalMap.size()));
            SignalState signalState = stateMap.get(signalName);
            SignalTransition.Type type = (signalState == null) ? null : signalState.type;
            signal.setType(convertSignalType(type));
            signalMap.put(signalName, signal);
        }
        double x = 0.0;
        for (String transitionName: trace) {
            Node node = stg.getNodeByReference(transitionName);
            if (node instanceof DummyTransition) {
                HashSet<SignalEvent> propagatedCauses = new HashSet<>();
                for (Node pred: stg.getPreset(node)) {
                    propagatedCauses.addAll(causeMap.get(pred));
                    causeMap.remove(pred);
                }
                for (Node succ: stg.getPostset(node)) {
                    causeMap.put(succ, propagatedCauses);
                }
            } else if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalName = transition.getSignalName();
                VisualSignal signal = signalMap.get(signalName);
                Direction direction =  getDirection(transition.getDirection());
                SignalEvent curEvent = dtd.appendSignalEvent(signal, direction);
                x += 2.0;
                curEvent.edge.setX(x);
                for (Node pred: stg.getPreset(transition)) {
                    HashSet<SignalEvent> predEvents = causeMap.get(pred);
                    if (predEvents == null) continue;
                    for (SignalEvent predEvent: predEvents) {
                        try {
                            dtd.connect(predEvent.edge, curEvent.edge);
                        } catch (InvalidConnectionException e) {
                        }
                    }
                    causeMap.remove(pred);
                }
                for (Node succ: stg.getPostset(transition)) {
                    HashSet<SignalEvent> signalEvents = new HashSet<>();
                    signalEvents.add(curEvent);
                    causeMap.put(succ, signalEvents);
                }
            }
        }
        return dtd;
    }

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
        HashMap<String, SignalState> newStateMap = new HashMap<>(signalSet.size());
        for (String signal: signalSet) {
            SignalState signalState = new SignalState(signal, stg.getSignalType(signal));
            signalState.copy(stateMap.get(signal));
            newStateMap.put(signal, signalState);
        }
        stateMap = newStateMap;

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
