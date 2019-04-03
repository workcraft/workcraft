package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.InitialisationState;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Function;

public class InitialisationAnalyserTool extends AbstractGraphEditorTool {

    private JTable forceTable;
    private InitialisationState initState;
    private final ArrayList<String> forcedPins = new ArrayList<>();

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
        ColorLegendTableModel legendTableModel = new ColorLegendTableModel(Arrays.asList(
                Pair.of(CommonVisualSettings.getFillColor(), "Undefined initial state"),
                Pair.of(CircuitSettings.getConflictInitGateColor(), "Conflict of initialisation"),
                Pair.of(CircuitSettings.getForcedInitGateColor(), "Forced initial state"),
                Pair.of(CircuitSettings.getPropagatedInitGateColor(), "Propagated initial state")
        ));

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
        TableColumn colorLegendColumn = columnModel.getColumn(0);
        colorLegendColumn.setMinWidth(colorCellSize);
        colorLegendColumn.setMaxWidth(colorCellSize);

        JPanel legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBorder(SizeHelper.getTitledBorder("Gate highlight legend"));
        legendPanel.add(legendTable, BorderLayout.CENTER);
        return legendPanel;
    }

    private JPanel getForcedControlsPanel(final GraphEditor editor) {
        BasicTableModel<String> forceTableModel = new BasicTableModel(forcedPins);
        forceTable = new JTable(forceTableModel);
        forceTable.setFocusable(false);
        forceTable.setRowSelectionAllowed(false);
        forceTable.setRowHeight(SizeHelper.getComponentHeightFromFont(forceTable.getFont()));
        forceTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        forceTable.setTableHeader(null);
        JScrollPane forceScrollPane = new JScrollPane(forceTable);

        JButton tagForceInitInputPortsButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-initialisation-input_ports.svg"),
                "Force init all input ports (environment responsibility)");
        tagForceInitInputPortsButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.tagForceInitInputPorts(c)));

        JButton tagForceInitNecessaryPinsButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-initialisation-conflict_pins.svg"),
                "Force init pins with conflicting initial state");
        tagForceInitNecessaryPinsButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.tagForceInitConflictPins(c)));

        JButton tagForceInitAutoAppendButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-initialisation-auto_append.svg"),
                "Auto-append force init pins as necessary to complete initialisation");
        tagForceInitAutoAppendButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.tagForceInitAutoAppend(c)));

        JButton tagForceInitAutoDiscardButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-initialisation-auto_discard.svg"),
                "Auto-discard force init pins that are redundant for initialisation");
        tagForceInitAutoDiscardButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.tagForceInitAutoDiscard(c)));

        JButton tagForceInitClearAllButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-initialisation-clear_all.svg"),
                "Clear all force init ports and pins");
        tagForceInitClearAllButton.addActionListener(l -> changeForceInit(editor, c -> ResetUtils.tagForceInitClearAll(c)));

        FlowLayout flowLayout = new FlowLayout();
        int buttonWidth = (int) Math.round(tagForceInitInputPortsButton.getPreferredSize().getWidth() + flowLayout.getHgap());
        int buttonHeight = (int) Math.round(tagForceInitInputPortsButton.getPreferredSize().getHeight() + flowLayout.getVgap());
        Dimension panelSize = new Dimension(buttonWidth * 5 + flowLayout.getHgap(), buttonHeight + flowLayout.getVgap());

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(flowLayout);
        btnPanel.setPreferredSize(panelSize);
        btnPanel.setMaximumSize(panelSize);
        btnPanel.add(tagForceInitInputPortsButton);
        btnPanel.add(tagForceInitNecessaryPinsButton);
        btnPanel.add(tagForceInitAutoAppendButton);
        btnPanel.add(tagForceInitAutoDiscardButton);
        btnPanel.add(tagForceInitClearAllButton);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.setBorder(SizeHelper.getTitledBorder("Force init pins"));
        forcePanel.add(forceScrollPane, BorderLayout.CENTER);
        forcePanel.add(btnPanel, BorderLayout.SOUTH);
        return forcePanel;
    }

    private void changeForceInit(final GraphEditor editor, Function<Circuit, Collection<? extends Contact>> func) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.captureMemento();
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        Collection<? extends Contact> changedContacts = func.apply(circuit);
        if (changedContacts.isEmpty()) {
            we.uncaptureMemento();
        } else {
            we.saveMemento();
        }
        circuit = (Circuit) editor.getModel().getMathModel();
        updateState(circuit);
        editor.requestFocus();
    }

    private JPanel getResetControlsPanel(final GraphEditor editor) {
        JButton insertResetButton = new JButton("Insert reset logic...");
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
        return GuiUtils.createIconFromSVG("images/circuit-tool-initialisation_analysis.svg");
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
        forcedPins.clear();
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
        forcedPins.clear();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getForcedInit()) {
                String pinRef = circuit.getNodeReference(contact);
                forcedPins.add(pinRef);
            }
        }
        Collections.sort(forcedPins);
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
                FunctionContact mathContact = ((VisualFunctionContact) contact).getReferencedContact();
                editor.getWorkspaceEntry().saveMemento();
                mathContact.setForcedInit(!mathContact.getForcedInit());
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
                MathNode mathNode = null;
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

    private Decoration getLowLevelDecoration(MathNode node) {
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

    private Decoration getHighLevelDecoration(MathNode node) {
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
