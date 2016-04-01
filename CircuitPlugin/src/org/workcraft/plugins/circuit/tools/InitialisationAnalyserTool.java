package org.workcraft.plugins.circuit.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanUtils;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;

public class InitialisationAnalyserTool extends AbstractTool {

    private static final int COLUMN_SIGNAL = 0;

    private static final Color COLOR_CONFLICT = new Color(1.0f, 0.7f, 0.7f);
    private static final Color COLOR_INITIALISED = new Color(0.7f, 1.0f, 0.7f);

    protected JPanel interfacePanel;
    protected JPanel controlPanel;
    protected JScrollPane infoPanel;
    protected JPanel statusPanel;
    private JTable signalTable;

    private Circuit circuit;
    private ArrayList<String> signals;
    private HashSet<Node> initHighSet;
    private HashSet<Node> initLowSet;
    private HashSet<Node> initErrorSet;

    @Override
    public String getLabel() {
        return "Initialisisation analiser";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_I;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/icons/svg/tool-initialisation_analysis.svg");
    }

    @Override
    public void activated(final GraphEditor editor) {
        circuit = (Circuit) editor.getModel().getMathModel();
        signals = getSignals();
        updateState(circuit);
        super.activated(editor);
    }

    private ArrayList<String> getSignals() {
        ArrayList<String> result = new ArrayList<>();
        if (circuit != null) {
            for (Contact contact: circuit.getFunctionContacts()) {
                if (contact.isDriver()) {
                    String ref = circuit.getNodeReference(contact);
                    result.add(ref);
                }
            }
        }
        return result;
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        signals = null;
        initHighSet = null;
        initLowSet = null;
        initErrorSet = null;
    }

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        controlPanel = new JPanel();
        signalTable = new JTable(new SignalTableModel());
        signalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        signalTable.setDefaultRenderer(Object.class, new SignalTableCellRendererImplementation());
        signalTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        signalTable.setAutoCreateColumnsFromModel(false);
        infoPanel = new JScrollPane(signalTable);
        statusPanel = new JPanel();

        interfacePanel = new JPanel();
        interfacePanel.setLayout(new BorderLayout());
        interfacePanel.add(controlPanel, BorderLayout.PAGE_START);
        interfacePanel.add(infoPanel, BorderLayout.CENTER);
        interfacePanel.add(statusPanel, BorderLayout.PAGE_END);
        interfacePanel.setPreferredSize(new Dimension(0, 0));
    }

    @Override
    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    private void updateState(Circuit circuit) {
        initHighSet = new HashSet<>();
        initLowSet = new HashSet<>();
        initErrorSet = new HashSet<>();
        Queue<Connection> queue = new LinkedList<>();
        for (FunctionContact contact: circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getInitialised()) {
                HashSet<Node> init = contact.getInitToOne() ? initHighSet : initLowSet;
                if (init.add(contact)) {
                    Set<Connection> connections = circuit.getConnections(contact);
                    queue.addAll(connections);
                }
            }
        }
        while (!queue.isEmpty()) {
            Connection connection = queue.remove();
            Node fromNode = connection.getFirst();
            HashSet<Node> nodeInitLevelSet = chooseNodeLevelSet(fromNode, initHighSet, initLowSet);
            if ((nodeInitLevelSet != null) && nodeInitLevelSet.add(connection)) {
                if (initErrorSet.contains(fromNode)) {
                    initErrorSet.add(connection);
                }
                Node toNode = connection.getSecond();
                if (nodeInitLevelSet.add(toNode)) {
                    Node parent = toNode.getParent();
                    if (parent instanceof FunctionComponent) {
                        LinkedList<BooleanVariable> variables = new LinkedList<>();
                        LinkedList<BooleanFormula> values = new LinkedList<>();
                        LinkedList<FunctionContact> outputPins = new LinkedList<>();
                        for (FunctionContact contact: Hierarchy.getChildrenOfType(parent, FunctionContact.class)) {
                            if (contact.isOutput()) {
                                outputPins.add(contact);
                            }
                            HashSet<Node> contactInitLevelSet = chooseNodeLevelSet(contact, initHighSet, initLowSet);
                            if (contactInitLevelSet != null) {
                                variables.add(contact);
                                values.add(contactInitLevelSet == initHighSet ? One.instance() : Zero.instance());
                            }
                        }
                        for (FunctionContact outputPin: outputPins) {
                            Set<Node> outputInitLevelSet = chooseFunctionLevelSet(outputPin, variables, values, initHighSet, initLowSet);
                            if ((outputInitLevelSet != null) && outputInitLevelSet.add(outputPin)) {
                                if ((outputInitLevelSet == initHighSet) != (outputPin.getInitToOne())) {
                                    initErrorSet.add(outputPin);
                                }
                                Set<Connection> connections = circuit.getConnections(outputPin);
                                queue.addAll(connections);
                            }
                        }
                    } else {
                        Set<Connection> connections = circuit.getConnections(toNode);
                        queue.addAll(connections);
                    }
                }
            }
        }
        signalTable.tableChanged(null);
    }

    private HashSet<Node> chooseNodeLevelSet(Node node, HashSet<Node> highSet, HashSet<Node> lowSet) {
        if (highSet.contains(node)) {
            return highSet;
        }
        if (lowSet.contains(node)) {
            return lowSet;
        }
        return null;
    }

    private HashSet<Node> chooseFunctionLevelSet(FunctionContact contact, LinkedList<BooleanVariable> variables,
            LinkedList<BooleanFormula> values, HashSet<Node> highSet, HashSet<Node> lowSet) {
        BooleanFormula setFunction = BooleanUtils.cleverReplace(contact.getSetFunction(), variables, values);
        BooleanFormula resetFunction = BooleanUtils.cleverReplace(contact.getResetFunction(), variables, values);
        if (isEvaluatedHigh(setFunction, resetFunction)) {
            return highSet;
        } else if (isEvaluatedLow(setFunction, resetFunction)) {
            return lowSet;
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
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
            if (node instanceof VisualContact) {
                editor.getWorkspaceEntry().saveMemento();
                Contact contact = ((VisualContact) node).getReferencedContact();
                contact.setInitialised(!contact.getInitialised());
                Circuit circuit = (Circuit) editor.getModel().getMathModel();
                updateState(circuit);
                processed = true;
            }
        }

        if (!processed) {
            super.mouseClicked(e);
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
                    final boolean b = (initErrorSet != null) && initErrorSet.contains(mathNode);
                    if ((initHighSet != null) && initHighSet.contains(mathNode)) {
                        return new StateDecoration() {
                            @Override
                            public Color getColorisation() {
                                return CircuitSettings.getActiveWireColor();
                            }
                            @Override
                            public Color getBackground() {
                                return b ? CircuitSettings.getInactiveWireColor() : CircuitSettings.getActiveWireColor();
                            }
                        };
                    }
                    if ((initLowSet != null) && initLowSet.contains(mathNode)) {
                        return new StateDecoration() {
                            @Override
                            public Color getColorisation() {
                                return CircuitSettings.getInactiveWireColor();
                            }
                            @Override
                            public Color getBackground() {
                                return b ? CircuitSettings.getActiveWireColor() : CircuitSettings.getInactiveWireColor();
                            }
                        };
                    }
                }
                return (mathNode instanceof Contact) ? StateDecoration.Empty.INSTANCE : null;
            }
        };
    }
    @SuppressWarnings("serial")
    private final class SignalTableCellRendererImplementation implements TableCellRenderer {
        private final JLabel label = new JLabel() {
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
            if ((signals != null) && (row >= 0) && (row < signals.size())) {
                String ref = signals.get(row);
                Node node = circuit.getNodeByReference(ref);
                label.setText(ref);
                Color color = Color.WHITE;
                if (initHighSet.contains(node) || initLowSet.contains(node)) {
                    color = initErrorSet.contains(node) ? COLOR_CONFLICT : COLOR_INITIALISED;
                }
                label.setBackground(color);
                result = label;
            }
            return result;
        }
    }

    @SuppressWarnings("serial")
    private final class SignalTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int column) {
            return (column == 0) ? "Signal" : "";
        }

        @Override
        public int getRowCount() {
            return (signals != null) ? signals.size() : 0;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Object result = null;
            String ref = signals.get(row);
            switch (col) {
            case COLUMN_SIGNAL:
                result = ref;
                break;
            default:
                result = null;
                break;
            }
            return result;
        }
    }

}
