package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.controls.FlatHeaderRenderer;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.InitialisationState;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.plugins.circuit.utils.StatsUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public class InitialisationAnalyserTool extends AbstractGraphEditorTool {

    private static final String WARNING_SYMBOL = Character.toString((char) 0x26A0);
    private static final String WARNING_PREFIX = WARNING_SYMBOL + ' ';

    private InitialisationState initState = null;
    private final PortTable portTable = new PortTable();
    private final PinTable pinTable = new PinTable();

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getLegendControlsPanel(), BorderLayout.NORTH);
        panel.add(getForcedControlsPanel(editor), BorderLayout.CENTER);
        panel.add(getResetControlsPanel(editor), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private JPanel getLegendControlsPanel() {
        ColorLegendTable colorLegendTable = new ColorLegendTable(Arrays.asList(
                Pair.of(AnalysisDecorationSettings.getDontTouchColor(), "Don't touch zero delay"),
                Pair.of(AnalysisDecorationSettings.getProblemColor(), "Problem of initialisation"),
                Pair.of(VisualCommonSettings.getFillColor(), "Unknown initial state"),
                Pair.of(AnalysisDecorationSettings.getFixerColor(), "Forced initial state"),
                Pair.of(AnalysisDecorationSettings.getClearColor(), "Propagated initial state")
        ));

        String expectedPinLegend = getHtmlPinLegend("&#x2610;", "Expected high / low");
        String propagatedPinLegend = getHtmlPinLegend("&#x25A0;", "Propagated high / low");
        String forcedPinLegend = getHtmlPinLegend("&#x25C6;", "Forced high / low");
        JLabel legendLabel = new JLabel("<html><b>Pin initial state:</b><br>" + expectedPinLegend + "<br>" + propagatedPinLegend + "<br>" + forcedPinLegend + "</html>");
        JPanel legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBorder(GuiUtils.getTitledBorder("<html><b>Gate highlight legend</b></html>"));
        legendPanel.add(colorLegendTable, BorderLayout.CENTER);
        legendPanel.add(legendLabel, BorderLayout.SOUTH);
        return legendPanel;
    }

    private String getHtmlPinLegend(String key, String description) {
        String highKey = TextUtils.getHtmlSpanColor(key, CircuitSettings.getActiveWireColor());
        String lowKey = TextUtils.getHtmlSpanColor(key, CircuitSettings.getInactiveWireColor());
        return highKey + " / " + lowKey + ' ' + description;
    }

    private JPanel getForcedControlsPanel(final GraphEditor editor) {
        JButton tagForcedInitInputPortsButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-input_ports.svg",
                "Force init all input ports (environment responsibility)",
                l -> changeForcedInit(editor, ResetUtils::tagForcedInitInputPorts));

        JButton tagForcedInitNecessaryPinsButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-problematic_pins.svg",
                "Force init output pins with problematic initial state",
                l -> changeForcedInit(editor, ResetUtils::tagForcedInitProblematicPins));

        JButton tagForcedInitSequentialPinsButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-sequential_pins.svg",
                "Force init output pins of sequential gates",
                l -> changeForcedInit(editor, ResetUtils::tagForcedInitSequentialPins));

        JButton tagForcedInitAutoAppendButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-auto_append.svg",
                "Auto-append forced init pins as necessary to complete initialisation",
                l -> changeForcedInit(editor, ResetUtils::tagForcedInitAutoAppend));

        JButton tagForcedInitAutoDiscardButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-auto_discard.svg",
                "Auto-discard forced init pins that are redundant for initialisation",
                l -> changeForcedInit(editor, ResetUtils::tagForcedInitAutoDiscard));

        JButton tagForcedInitClearAllButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-clear_all.svg",
                "Clear all forced init ports and pins",
                l -> changeForcedInit(editor, ResetUtils::tagForcedInitClearAll));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(tagForcedInitInputPortsButton);
        buttonPanel.add(tagForcedInitNecessaryPinsButton);
        buttonPanel.add(tagForcedInitSequentialPinsButton);
        buttonPanel.add(tagForcedInitAutoAppendButton);
        buttonPanel.add(tagForcedInitAutoDiscardButton);
        buttonPanel.add(tagForcedInitClearAllButton);
        GuiUtils.setButtonPanelLayout(buttonPanel, tagForcedInitInputPortsButton.getPreferredSize());

        JPanel controlPanel = new JPanel(new WrapLayout());
        controlPanel.add(buttonPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(portTable), new JScrollPane(pinTable));

        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.add(controlPanel, BorderLayout.NORTH);
        forcePanel.add(splitPane, BorderLayout.CENTER);
        return forcePanel;
    }

    private void changeForcedInit(final GraphEditor editor, Function<Circuit, Collection<? extends Contact>> func) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.captureMemento();
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        Collection<? extends Contact> changedContacts = func.apply(circuit);
        if (changedContacts.isEmpty()) {
            we.uncaptureMemento();
        } else {
            we.saveMemento();
        }
        editor.requestFocus();
    }

    private JPanel getResetControlsPanel(final GraphEditor editor) {
        JButton insertHighResetButton = new JButton("<html><center>Insert reset<br>(active-high)</center></html>");
        insertHighResetButton.addActionListener(l -> insertReset(editor, false));

        JButton insertLowResetButton = new JButton("<html><center>Insert reset<br>(active-low)</center></html>");
        insertLowResetButton.addActionListener(l -> insertReset(editor, true));

        JPanel resetPanel = new JPanel(new WrapLayout());
        resetPanel.add(insertHighResetButton);
        resetPanel.add(insertLowResetButton);
        return resetPanel;
    }

    private void insertReset(final GraphEditor editor, boolean isActiveLow) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        we.captureMemento();
        if (ResetUtils.insertReset(circuit, isActiveLow)) {
            we.saveMemento();
        }
        we.uncaptureMemento();
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
        return "Click on a driver contact to toggle its forced init state.";
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        CircuitUtils.correctInitialState(circuit);
        updateState(editor);
        editor.getWorkspaceEntry().addObserver(e -> updateState(editor));
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        initState = null;
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(true);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    private void updateState(final GraphEditor editor) {
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        initState = new InitialisationState(circuit);
        portTable.refresh();
        pinTable.refresh();
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

            if ((contact != null) && contact.isDriver() && !contact.isZeroDelayPin()) {
                editor.getWorkspaceEntry().saveMemento();
                contact.getReferencedComponent().setForcedInit(!contact.getReferencedComponent().getForcedInit());
                processed = true;
            }
        }
        if (!processed) {
            super.mousePressed(e);
        }
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            MathNode mathNode = null;
            if (node instanceof VisualComponent) {
                mathNode = ((VisualComponent) node).getReferencedComponent();
            } else if (node instanceof VisualConnection) {
                mathNode = ((VisualConnection) node).getReferencedConnection();
            }

            if (mathNode != null) {
                if (mathNode instanceof FunctionComponent) {
                    return getComponentDecoration((FunctionComponent) mathNode);
                } else if (mathNode instanceof Contact) {
                    Circuit circuit = (Circuit) editor.getModel().getMathModel();
                    Contact driver = CircuitUtils.findDriver(circuit, mathNode, false);
                    return getContactDecoration(driver);
                } else {
                    return getConnectionDecoration(mathNode);
                }
            }
            return null;
        };
    }

    private Decoration getComponentDecoration(FunctionComponent component) {
        boolean forcedInit = false;
        boolean initialised = true;
        boolean hasProblem = false;
        for (Contact contact : component.getOutputs()) {
            forcedInit |= contact.getForcedInit();
            initialised &= initState.isInitialisedPin(contact);
            hasProblem |= initState.isProblematicPin(contact);
        }
        final Color color = component.getIsZeroDelay() ? AnalysisDecorationSettings.getDontTouchColor()
                : hasProblem ? AnalysisDecorationSettings.getProblemColor()
                : forcedInit ? AnalysisDecorationSettings.getFixerColor()
                : initialised ? AnalysisDecorationSettings.getClearColor() : null;

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

    private Decoration getContactDecoration(Contact contact) {
        if (initState.isHigh(contact)) {
            return getHighLevelDecoration(contact);
        }
        if (initState.isLow(contact)) {
            return getLowLevelDecoration(contact);
        }
        return getExpectedLevelDecoration(contact);
    }

    private Decoration getConnectionDecoration(MathNode node) {
        Color color = initState.isHigh(node) ? CircuitSettings.getActiveWireColor()
                : initState.isLow(node) ? CircuitSettings.getInactiveWireColor() : null;
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
        final boolean initialisationConflict = initState.isConflict(node);
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                return initialisationConflict ? CircuitSettings.getActiveWireColor() : CircuitSettings.getInactiveWireColor();
            }

            @Override
            public Color getBackground() {
                return CircuitSettings.getInactiveWireColor();
            }

            @Override
            public boolean showForcedInit() {
                return true;
            }
        };
    }

    private Decoration getHighLevelDecoration(MathNode node) {
        final boolean initialisationConflict = initState.isConflict(node);
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                return initialisationConflict ? CircuitSettings.getInactiveWireColor() : CircuitSettings.getActiveWireColor();
            }

            @Override
            public Color getBackground() {
                return CircuitSettings.getActiveWireColor();
            }

            @Override
            public boolean showForcedInit() {
                return true;
            }
        };
    }

    private Decoration getExpectedLevelDecoration(Contact contact) {
        return new StateDecoration() {
            @Override
            public Color getColorisation() {
                return (contact == null) ? null
                        : contact.getInitToOne() ? CircuitSettings.getActiveWireColor()
                        : CircuitSettings.getInactiveWireColor();
            }

            @Override
            public Color getBackground() {
                return null;
            }

            @Override
            public boolean showForcedInit() {
                return true;
            }
        };
    }

    private class PortTable extends JTable {

        PortTable() {
            setModel(new PortTableModel());
            setFocusable(false);
            setRowSelectionAllowed(false);
            setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            getTableHeader().setDefaultRenderer(new FlatHeaderRenderer(false));
            getTableHeader().setReorderingAllowed(false);
            setDefaultRenderer(Object.class, new PortTableCellRenderer());
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Contact contact = initState.getDriverPort(getSelectedRow());
                    if (contact != null) {
                        contact.setForcedInit(!contact.getForcedInit());
                    }
                }
            });
        }

        public void refresh() {
            int forcedPortCount = initState.getForcedPorts().size();
            int uninitialisedPortCount = initState.getDriverPortCount() - forcedPortCount;

            String header = StatsUtils.getHtmlStatsHeader("Input port assumptions",
                    null, uninitialisedPortCount, forcedPortCount, null);

            GuiUtils.setColumnHeader(this, 0, header);

            Color color = uninitialisedPortCount > 0 ? GuiUtils.getTableHeaderBackgroundColor()
                    : forcedPortCount > 0 ? AnalysisDecorationSettings.getFixerColor()
                    : GuiUtils.getTableHeaderBackgroundColor();

            getTableHeader().setBackground(color);
            GuiUtils.refreshTable(this);
        }
    }

    private class PortTableCellRenderer implements TableCellRenderer {

        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            JLabel result = null;
            if (value instanceof Contact) {
                Contact contact = (Contact) value;
                String ref = initState.getContactReference(contact);
                label.setText(ref);

                Color color = contact.getForcedInit() ? AnalysisDecorationSettings.getFixerColor() : null;
                label.setBackground(ColorUtils.colorise(GuiUtils.getTableCellBackgroundColor(), color));

                boolean fits = GuiUtils.getLabelTextWidth(label) < GuiUtils.getTableColumnTextWidth(table, col);
                label.setToolTipText(fits ? null : label.getText());
                label.setBorder(GuiUtils.getTableCellBorder());
                result = label;
            }
            return result;
        }
    }

    private class PortTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return initState.getDriverPortCount();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return initState.getDriverPort(row);
        }
    }

    private class PinTable extends JTable {

        PinTable() {
            setModel(new PinTableModel());
            setFocusable(false);
            setRowSelectionAllowed(false);
            setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setDefaultRenderer(new FlatHeaderRenderer(false));
            setDefaultRenderer(Object.class, new PinTableCellRenderer());
            GuiUtils.setColumnWidth(this, 1, 30);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Contact contact = initState.getDriverPin(getSelectedRow());
                    if (contact != null) {
                        contact.setForcedInit(!contact.getForcedInit());
                    }
                }
            });
        }

        public void refresh() {
            int problematicPinCount = initState.getProblematicPins().size();
            int uninitialisedPinCount = initState.getUninitialisedPins().size();
            int forcedPinCount = initState.getForcedPins().size();
            int clearPinCount = initState.getDriverPinCount() - uninitialisedPinCount;

            String header = StatsUtils.getHtmlStatsHeader("Driver pins",
                    problematicPinCount, uninitialisedPinCount, forcedPinCount, clearPinCount);

            GuiUtils.setColumnHeader(this, 0, header);

            final Color color = problematicPinCount > 0 ? AnalysisDecorationSettings.getProblemColor()
                    : uninitialisedPinCount > 0 ? GuiUtils.getTableHeaderBackgroundColor()
                    : forcedPinCount > 0 ? AnalysisDecorationSettings.getFixerColor()
                    : clearPinCount > 0 ? AnalysisDecorationSettings.getClearColor()
                    : GuiUtils.getTableHeaderBackgroundColor();

            getTableHeader().setBackground(color);
            GuiUtils.refreshTable(this);
        }
    }

    private class PinTableCellRenderer implements TableCellRenderer {

        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            JLabel result = null;
            if (value instanceof FunctionContact) {
                FunctionContact contact = (FunctionContact) value;
                String ref = initState.getContactReference(contact);
                String text = initState.isRedundantForcedInit(contact) ? WARNING_PREFIX + ref : ref;
                label.setText(text);

                Color color = initState.isProblematicPin(contact) ? AnalysisDecorationSettings.getProblemColor()
                        : !initState.isInitialisedPin(contact) ? VisualCommonSettings.getFillColor()
                        : contact.isForcedDriver() ? AnalysisDecorationSettings.getFixerColor()
                        : initState.isInitialisedPin(contact) ? AnalysisDecorationSettings.getClearColor()
                        : null;

                label.setBackground(ColorUtils.colorise(GuiUtils.getTableCellBackgroundColor(), color));

                boolean fits = GuiUtils.getLabelTextWidth(label) < GuiUtils.getTableColumnTextWidth(table, col);
                label.setToolTipText(fits ? null : label.getText());
                label.setBorder(GuiUtils.getTableCellBorder());
                result = label;
            }
            return result;
        }
    }

    private class PinTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return initState.getDriverPinCount();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return initState.getDriverPin(row);
        }
    }

}
