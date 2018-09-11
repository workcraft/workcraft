package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractGraphEditorTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.plugins.circuit.*;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.Queue;

public class InitialisationAnalyserTool extends AbstractGraphEditorTool {

    private static final int COLUMN_COLOR = 0;
    private static final int COLUMN_DESCRIPTION = 1;
    private static final int ROW_INIT_CONFLICT = 0;
    private static final int ROW_INIT_FORCED = 1;
    private static final int ROW_INIT_PROPAGATED = 2;

    private final HashSet<Node> initHighSet = new HashSet<>();
    private final HashSet<Node> initLowSet = new HashSet<>();
    private final HashSet<Node> initErrorSet = new HashSet<>();
    private final ArrayList<String> initForcedPins = new ArrayList<>();

    private JTable forceTable;

    private final class ColorDataRenderer implements TableCellRenderer {
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
            label.setBackground((Color) value);
            return label;
        }
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        LegendTableModel legendTableModel = new LegendTableModel();
        JTable legendTable = new JTable(legendTableModel);
        legendTable.setFocusable(false);
        legendTable.setRowSelectionAllowed(false);
        legendTable.setRowHeight(SizeHelper.getComponentHeightFromFont(legendTable.getFont()));
        legendTable.setDefaultRenderer(Color.class, new ColorDataRenderer());
        legendTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumnModel columnModel = legendTable.getColumnModel();
        columnModel.getColumn(COLUMN_COLOR).setPreferredWidth(50);
        columnModel.getColumn(COLUMN_DESCRIPTION).setPreferredWidth(200);

        JPanel legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBorder(SizeHelper.getTitledBorder("Gate highlight legend"));
        legendPanel.add(legendTable, BorderLayout.CENTER);

        ForceTableModel forceTableModel = new ForceTableModel();
        forceTable = new JTable(forceTableModel);
        forceTable.setFocusable(false);
        forceTable.setRowSelectionAllowed(false);
        forceTable.setRowHeight(SizeHelper.getComponentHeightFromFont(forceTable.getFont()));
        forceTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        forceTable.setTableHeader(null);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.setBorder(SizeHelper.getTitledBorder("Force init pins"));

        JScrollPane forceScrollPane = new JScrollPane(forceTable);
        forceScrollPane.setMinimumSize(new Dimension(1, 50));
        forcePanel.add(forceScrollPane, BorderLayout.CENTER);

        JButton toggleForceInitButton = new JButton("Toggle force init for all inputs");
        toggleForceInitButton.addActionListener(l -> toggleForceInit(editor));
        forcePanel.add(toggleForceInitButton, BorderLayout.SOUTH);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        namePanel.add(new JLabel("Port name:"));
        namePanel.add(new JTextField("nrst"));
        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        levelPanel.add(new JLabel("Active level:"));
        JRadioButton lowRadio = new JRadioButton("low");
        JRadioButton highRadio = new JRadioButton("high");
        levelPanel.add(lowRadio);
        levelPanel.add(highRadio);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(lowRadio);
        buttonGroup.add(highRadio);
        lowRadio.setSelected(true);

        JButton insertResetButton = new JButton("Insert");
        insertResetButton.addActionListener(l -> insertReset(editor));

        JPanel resetPanel = new JPanel(new WrapLayout());
        resetPanel.setBorder(SizeHelper.getTitledBorder("Reset logic insertion"));
        resetPanel.add(namePanel);
        resetPanel.add(levelPanel);
        resetPanel.add(insertResetButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(legendPanel, BorderLayout.NORTH);
        panel.add(forcePanel, BorderLayout.CENTER);
        panel.add(resetPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private void toggleForceInit(GraphEditor editor) {
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        boolean allForceInit = true;
        for (Contact port : circuit.getInputPorts()) {
            if (!port.getForcedInit()) {
                allForceInit = false;
                break;
            }
        }
        for (Contact port : circuit.getInputPorts()) {
            port.setForcedInit(!allForceInit);
        }
        updateState(circuit);
    }

    private void insertReset(GraphEditor editor) {
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        HashSet<String> r = new HashSet<>();
        for (Contact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver()) {
                if (!initLowSet.contains(contact) && !initHighSet.contains(contact)) {
                    String ref = circuit.getNodeReference(contact);
                    r.add(ref);
                }
            }
        }
        if (!r.isEmpty()) {
            String msg = "All gates must be correctly initialised before inserting reset.\n" +
                    LogUtils.getTextWithRefs("Problematic signal", r);
            DialogUtils.showError(msg);
        }
    }

    private final class LegendTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(final int col) {
            switch (col) {
            case COLUMN_COLOR:
                return Color.class;
            case COLUMN_DESCRIPTION:
                return String.class;
            default:
                return null;
            }
        }

        @Override
        public int getRowCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (row) {
            case ROW_INIT_CONFLICT:
                return (col == COLUMN_COLOR) ? CircuitSettings.getConflictInitGateColor() : "Conflict of initialisation";
            case ROW_INIT_FORCED:
                return (col == COLUMN_COLOR) ? CircuitSettings.getForcedInitGateColor() : "Forced initial state";
            case ROW_INIT_PROPAGATED:
                return (col == COLUMN_COLOR) ? CircuitSettings.getPropagatedInitGateColor() : "Propagated initial state";
            default:
                return null;
            }
        }
    }

    private final class ForceTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
            case COLUMN_COLOR:
                return "<html><b>Forced pin</b></html>";
            default:
                return null;
            }
        }

        @Override
        public int getRowCount() {
            return initForcedPins.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return (row < initForcedPins.size()) ? initForcedPins.get(row) : null;
        }
    }

    @Override
    public String getLabel() {
        return "Initialisation analyser";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_I;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/circuit-tool-initialisation_analysis.svg");
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a driver contact to toggle its force initialisation state.";
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        updateState(circuit);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        initHighSet.clear();
        initLowSet.clear();
        initErrorSet.clear();
        initForcedPins.clear();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    private void updateState(Circuit circuit) {
        initHighSet.clear();
        initLowSet.clear();
        initErrorSet.clear();
        initForcedPins.clear();
        Queue<Connection> queue = new LinkedList<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getForcedInit()) {
                String pinRef = circuit.getNodeReference(contact);
                initForcedPins.add(pinRef);
                HashSet<Node> initSet = (contact.getInitToOne()) ? initHighSet : initLowSet;
                if (initSet.add(contact)) {
                    queue.addAll(circuit.getConnections(contact));
                }
            }
        }
        Collections.sort(initForcedPins);
        while (!queue.isEmpty()) {
            Connection connection = queue.remove();
            Node fromNode = connection.getFirst();
            HashSet<Node> nodeInitLevelSet = chooseNodeLevelSet(fromNode);
            if ((nodeInitLevelSet != null) && nodeInitLevelSet.add(connection)) {
                if (initErrorSet.contains(fromNode)) {
                    initErrorSet.add(connection);
                }
                Node toNode = connection.getSecond();
                if (nodeInitLevelSet.add(toNode)) {
                    Node parent = toNode.getParent();
                    if (parent instanceof FunctionComponent) {
                        FunctionComponent component = (FunctionComponent) parent;
                        propagateValuesToOutputs(circuit, component, queue);
                    } else {
                        Set<Connection> connections = circuit.getConnections(toNode);
                        queue.addAll(connections);
                    }
                }
            }
        }
        forceTable.tableChanged(new TableModelEvent(forceTable.getModel()));
    }

    private void fillVariableValues(FunctionComponent component,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {
        for (FunctionContact contact : component.getFunctionContacts()) {
            HashSet<Node> contactInitLevelSet = chooseNodeLevelSet(contact);
            if (contactInitLevelSet != null) {
                variables.add(contact);
                values.add(contactInitLevelSet == initHighSet ? One.instance() : Zero.instance());
            }
        }
    }

    private void propagateValuesToOutputs(Circuit circuit, FunctionComponent component, Queue<Connection> queue) {
        boolean progress = true;
        while (progress) {
            progress = false;
            LinkedList<BooleanVariable> variables = new LinkedList<>();
            LinkedList<BooleanFormula> values = new LinkedList<>();
            fillVariableValues(component, variables, values);
            for (FunctionContact outputPin : component.getFunctionOutputs()) {
                Set<Node> outputInitLevelSet = chooseFunctionLevelSet(outputPin, variables, values);
                if ((outputInitLevelSet != null) && outputInitLevelSet.add(outputPin)) {
                    progress = true;
                    if (!outputPin.getForcedInit() && ((outputInitLevelSet == initHighSet) != outputPin.getInitToOne())) {
                        initErrorSet.add(outputPin);
                    }
                    queue.addAll(circuit.getConnections(outputPin));
                }
            }
        }
    }

    private HashSet<Node> chooseNodeLevelSet(Node node) {
        if (initHighSet.contains(node)) {
            return initHighSet;
        }
        if (initLowSet.contains(node)) {
            return initLowSet;
        }
        return null;
    }

    private HashSet<Node> chooseFunctionLevelSet(FunctionContact contact,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {
        if (contact.getForcedInit()) {
            return contact.getInitToOne() ? initHighSet : initLowSet;
        }
        BooleanFormula setFunction = BooleanUtils.replaceClever(contact.getSetFunction(), variables, values);
        BooleanFormula resetFunction = BooleanUtils.replaceClever(contact.getResetFunction(), variables, values);
        if (isEvaluatedHigh(setFunction, resetFunction)) {
            return initHighSet;
        } else if (isEvaluatedLow(setFunction, resetFunction)) {
            return initLowSet;
        }
        return null;
    }

    private boolean isEvaluatedHigh(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return One.instance().equals(setFunction) && ((resetFunction == null) || Zero.instance().equals(resetFunction));
    }

    private boolean isEvaluatedLow(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return Zero.instance().equals(setFunction) && ((resetFunction == null) || One.instance().equals(resetFunction));
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        boolean processed = false;
        GraphEditor editor = e.getEditor();
        VisualModel model = e.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            Node deepestNode = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    node -> (node instanceof VisualFunctionComponent) || (node instanceof VisualContact));

            VisualContact contact = null;
            if (deepestNode instanceof VisualFunctionContact) {
                contact = (VisualFunctionContact) deepestNode;
            } else if (deepestNode instanceof VisualFunctionComponent) {
                VisualFunctionComponent component = (VisualFunctionComponent) deepestNode;
                contact = component.getMainVisualOutput();
            }

            if ((contact instanceof VisualFunctionContact) && contact.isDriver()) {
                FunctionContact funcContact = ((VisualFunctionContact) contact).getReferencedFunctionContact();
                editor.getWorkspaceEntry().saveMemento();
                funcContact.setForcedInit(!funcContact.getForcedInit());
                processed = true;
            }
        }
        if (processed) {
            Circuit circuit = (Circuit) model.getMathModel();
            updateState(circuit);
        } else {
            super.mousePressed(e);
        }
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                Node mathNode = null;
                if (node instanceof VisualComponent) {
                    mathNode = ((VisualComponent) node).getReferencedComponent();
                } else if (node instanceof VisualConnection) {
                    mathNode = ((VisualConnection) node).getReferencedConnection();
                }

                if (mathNode != null) {
                    if (mathNode instanceof FunctionComponent) {
                        return getComponentDecoration((FunctionComponent) mathNode);
                    }
                    if (initHighSet.contains(mathNode)) {
                        return getHighLevelDecoration(mathNode);
                    }
                    if (initLowSet.contains(mathNode)) {
                        return getLowLevelDecoration(mathNode);
                    }
                }
                return (mathNode instanceof Contact) ? StateDecoration.Empty.INSTANCE : null;
            }
        };
    }

    private Decoration getComponentDecoration(FunctionComponent component) {
        boolean forcedInit = false;
        boolean initialised = true;
        boolean initialisationConflict = false;
        for (Contact outputContact : component.getOutputs()) {
            forcedInit |= outputContact.getForcedInit();
            initialised &= initHighSet.contains(outputContact) || initLowSet.contains(outputContact);
            initialisationConflict |= initErrorSet.contains(outputContact);
        }
        final Color color = forcedInit ? CircuitSettings.getForcedInitGateColor()
                : initialisationConflict ? CircuitSettings.getConflictInitGateColor()
                : initialised ? CircuitSettings.getPropagatedInitGateColor() : null;

        return new Decoration() {
            @Override
            public Color getColorisation() {
                return color;
            }

            @Override
            public Color getBackground() {
                return color;
            }
        };
    }

    private Decoration getLowLevelDecoration(Node node) {
        final boolean initialisationConflict = initErrorSet.contains(node);
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                return CircuitSettings.getInactiveWireColor();
            }

            @Override
            public Color getBackground() {
                return initialisationConflict ? CircuitSettings.getActiveWireColor() : CircuitSettings.getInactiveWireColor();
            }

            @Override
            public boolean showForcedInit() {
                return true;
            }
        };
    }

    private Decoration getHighLevelDecoration(Node node) {
        final boolean initialisationConflict = initErrorSet.contains(node);
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                return CircuitSettings.getActiveWireColor();
            }

            @Override
            public Color getBackground() {
                return initialisationConflict ? CircuitSettings.getInactiveWireColor() : CircuitSettings.getActiveWireColor();
            }

            @Override
            public boolean showForcedInit() {
                return true;
            }
        };
    }

}
