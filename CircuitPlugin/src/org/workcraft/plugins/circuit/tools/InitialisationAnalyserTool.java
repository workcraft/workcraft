package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractGraphEditorTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.InitialisationState;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.function.Function;

public class InitialisationAnalyserTool extends AbstractGraphEditorTool {

    private static final int LEGEND_COLUMN_COLOR = 0;
    private static final int LEGEND_COLUMN_DESCRIPTION = 1;
    private static final int LEGEND_ROW_INIT_UNDEFINED = 0;
    private static final int LEGEND_ROW_INIT_CONFLICT = 1;
    private static final int LEGEND_ROW_INIT_FORCED = 2;
    private static final int LEGEND_ROW_INIT_PROPAGATED = 3;

    private JTable forceTable;
    private InitialisationState initState;

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getLegendControlsPanel(editor), BorderLayout.NORTH);
        panel.add(getForcedControlsPanel(editor), BorderLayout.CENTER);
        panel.add(getResetControlsPanel(editor), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private JPanel getLegendControlsPanel(final GraphEditor editor) {
        LegendTableModel legendTableModel = new LegendTableModel();
        JTable legendTable = new JTable(legendTableModel);
        legendTable.setFocusable(false);
        legendTable.setRowSelectionAllowed(false);
        legendTable.setRowHeight(SizeHelper.getComponentHeightFromFont(legendTable.getFont()));
        legendTable.setDefaultRenderer(Color.class, new ColorDataRenderer());
        legendTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        // Make the table transparent
        legendTable.setShowGrid(false);
        legendTable.setOpaque(false);
        DefaultTableCellRenderer legendRenderer = (DefaultTableCellRenderer) legendTable.getDefaultRenderer(Object.class);
        legendRenderer.setOpaque(false);
        // Set the color cells square shape
        TableColumnModel columnModel = legendTable.getColumnModel();
        int colorCellSize = legendTable.getRowHeight();
        TableColumn colorLegendColumn = columnModel.getColumn(LEGEND_COLUMN_COLOR);
        colorLegendColumn.setMinWidth(colorCellSize);
        colorLegendColumn.setMaxWidth(colorCellSize);

        JPanel legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBorder(SizeHelper.getTitledBorder("Gate highlight legend"));
        legendPanel.add(legendTable, BorderLayout.CENTER);
        return legendPanel;
    }

    private JPanel getForcedControlsPanel(final GraphEditor editor) {
        ForceTableModel forceTableModel = new ForceTableModel();
        forceTable = new JTable(forceTableModel);
        forceTable.setFocusable(false);
        forceTable.setRowSelectionAllowed(false);
        forceTable.setRowHeight(SizeHelper.getComponentHeightFromFont(forceTable.getFont()));
        forceTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        forceTable.setTableHeader(null);
        JScrollPane forceScrollPane = new JScrollPane(forceTable);

        JButton toggleForceInitInputsButton = GUI.createIconButton(
                GUI.createIconFromSVG("images/circuit-initialisation-force-input.svg"),
                "Toggle force init for all inputs");
        toggleForceInitInputsButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.toggleForceInitInputs(c)));

        JButton toggleForceInitLoopsButton = GUI.createIconButton(
                GUI.createIconFromSVG("images/circuit-initialisation-force-selfloop.svg"),
                "Toggle force init for all self-loops");
        toggleForceInitLoopsButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.toggleForceInitLoops(c)));

        JButton toggleForceInitCelementsButton = GUI.createIconButton(
                GUI.createIconFromSVG("images/circuit-initialisation-force-celement.svg"),
                "Toggle force init for all sequential gates");
        toggleForceInitCelementsButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.toggleForceInitSequentialGates(c)));

        JButton clearForceInitButton = GUI.createIconButton(
                GUI.createIconFromSVG("images/circuit-initialisation-force-autoclean.svg"),
                "Clear redundant force init from pins");
        clearForceInitButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.clearRedundantForceInitPins(c)));

        JButton completeForceInitButton = GUI.createIconButton(
                GUI.createIconFromSVG("images/circuit-initialisation-force-autocomplete.svg"),
                "Complete initialisation by adding force init to pins");
        completeForceInitButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.completeForceInitPins(c)));

        FlowLayout flowLayout = new FlowLayout();
        int buttonWidth = (int) Math.round(toggleForceInitInputsButton.getPreferredSize().getWidth() + flowLayout.getHgap());
        int buttonHeight = (int) Math.round(toggleForceInitInputsButton.getPreferredSize().getHeight() + flowLayout.getVgap());
        Dimension panelSize = new Dimension(buttonWidth * 5 + flowLayout.getHgap(), buttonHeight + flowLayout.getVgap());

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(flowLayout);
        btnPanel.setPreferredSize(panelSize);
        btnPanel.setMaximumSize(panelSize);
        btnPanel.add(toggleForceInitInputsButton);
        btnPanel.add(toggleForceInitLoopsButton);
        btnPanel.add(toggleForceInitCelementsButton);
        btnPanel.add(clearForceInitButton);
        btnPanel.add(completeForceInitButton);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.setBorder(SizeHelper.getTitledBorder("Force init pins"));
        forcePanel.add(forceScrollPane, BorderLayout.CENTER);
        forcePanel.add(btnPanel, BorderLayout.SOUTH);
        return forcePanel;
    }

    public void changeForceInit(final GraphEditor editor, Function<Circuit, HashSet<? extends Contact>> func) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.captureMemento();
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        HashSet<? extends Contact> changedContacts = func.apply(circuit);
        if (changedContacts.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
        }
        circuit = (Circuit) editor.getModel().getMathModel();
        updateState(circuit);
        editor.requestFocus();
    }

    private JPanel getResetControlsPanel(final GraphEditor editor) {
        JButton insertResetButton = new JButton("Insert reset logic");
        insertResetButton.addActionListener(l -> insertReset(editor));

        JPanel resetPanel = new JPanel();
        resetPanel.add(insertResetButton);
        return resetPanel;
    }

    private void insertReset(final GraphEditor editor) {
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        HashSet<String> incorrectlyInitialisedComponentRefs = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver()) {
                if (!initState.isCorrectlyInitialised(contact)) {
                    String ref = circuit.getNodeReference(contact);
                    incorrectlyInitialisedComponentRefs.add(ref);
                }
            }
        }
        if (!incorrectlyInitialisedComponentRefs.isEmpty()) {
            String msg = "All gates must be correctly initialised before inserting reset.\n" +
                    LogUtils.getTextWithRefs("Problematic signal", incorrectlyInitialisedComponentRefs);
            DialogUtils.showError(msg);
        } else {
            Object[] options1 = {"Insert active-low reset", "Insert active-high reset", "Cancel"};
            JPanel panel = new JPanel();
            panel.add(new JLabel("Reset port name: "));
            JTextField textField = new JTextField(CircuitSettings.getResetName(), 20);
            panel.add(textField);

            int result = JOptionPane.showOptionDialog(null, panel, "Reset logic",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);
            if ((result == JOptionPane.YES_OPTION) || (result == JOptionPane.NO_OPTION)) {
                editor.getWorkspaceEntry().saveMemento();
                VisualCircuit visualCircuit = (VisualCircuit) editor.getModel();
                ResetUtils.insertReset(visualCircuit, textField.getText(), result == JOptionPane.YES_OPTION);
                updateState(circuit);
            }
        }
        editor.requestFocus();
    }

    private final class LegendTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(final int col) {
            switch (col) {
            case LEGEND_COLUMN_COLOR:
                return Color.class;
            case LEGEND_COLUMN_DESCRIPTION:
                return String.class;
            default:
                return null;
            }
        }

        @Override
        public int getRowCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (row) {
            case LEGEND_ROW_INIT_UNDEFINED:
                return (col == LEGEND_COLUMN_COLOR) ? CommonVisualSettings.getFillColor() : "Undefined initial state";
            case LEGEND_ROW_INIT_CONFLICT:
                return (col == LEGEND_COLUMN_COLOR) ? CircuitSettings.getConflictInitGateColor() : "Conflict of initialisation";
            case LEGEND_ROW_INIT_FORCED:
                return (col == LEGEND_COLUMN_COLOR) ? CircuitSettings.getForcedInitGateColor() : "Forced initial state";
            case LEGEND_ROW_INIT_PROPAGATED:
                return (col == LEGEND_COLUMN_COLOR) ? CircuitSettings.getPropagatedInitGateColor() : "Propagated initial state";
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
        public int getRowCount() {
            return initState.getForcedPinCount();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return initState.getForcedPin(row);
        }
    }

    private final class ColorDataRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel() {
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
        initState = null;
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    private void updateState(Circuit circuit) {
        initState = new InitialisationState(circuit);
        forceTable.tableChanged(new TableModelEvent(forceTable.getModel()));
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
                    if (initState.isHigh(mathNode)) {
                        return getHighLevelDecoration(mathNode);
                    }
                    if (initState.isLow(mathNode)) {
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
            initialised &= initState.isHigh(outputContact) || initState.isLow(outputContact);
            initialisationConflict |= initState.isError(outputContact);
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
        final boolean initialisationConflict = initState.isError(node);
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
        final boolean initialisationConflict = initState.isError(node);
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
