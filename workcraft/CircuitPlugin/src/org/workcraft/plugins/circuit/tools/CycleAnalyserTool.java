package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.controls.FlatHeaderRenderer;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.tools.*;
import org.workcraft.interop.Format;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.interop.SdcFormat;
import org.workcraft.plugins.circuit.utils.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public class CycleAnalyserTool extends AbstractGraphEditorTool {

    private static final String WARNING_SYMBOL = Character.toString((char) 0x26A0);
    private static final String WARNING_PREFIX = WARNING_SYMBOL + ' ';

    private CycleState cycleState = null;
    private final DriverPinTable driverPinTable = new DriverPinTable();
    private final DrivenPinTable drivenPinTable = new DrivenPinTable();

    private JPanel panel;
    private JButton writeConstraintsButton;

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }
        panel = new JPanel(new BorderLayout());
        panel.add(getLegendControlsPanel(), BorderLayout.NORTH);
        panel.add(getBreakControlsPanel(editor), BorderLayout.CENTER);
        panel.add(getScanControlsPanel(editor), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private JPanel getLegendControlsPanel() {
        ColorLegendTable colorLegendTable = new ColorLegendTable(Arrays.asList(
                Pair.of(AnalysisDecorationSettings.getDontTouchColor(), "Don't touch zero delay"),
                Pair.of(AnalysisDecorationSettings.getProblemColor(), "On a cycle"),
                Pair.of(AnalysisDecorationSettings.getFixerColor(), "Path breaker"),
                Pair.of(AnalysisDecorationSettings.getClearColor(), "Not on any cycle")
        ));

        JPanel legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBorder(GuiUtils.getTitledBorder("<html><b>Highlight legend</b></html>"));
        legendPanel.add(colorLegendTable, BorderLayout.CENTER);
        return legendPanel;
    }

    private JPanel getBreakControlsPanel(final GraphEditor editor) {

        JButton tagPathBreakerSelfloopPinsButton = GuiUtils.createIconButton(
                "images/circuit-cycle-selfloop_pins.svg",
                "Path breaker all self-loops",
                l -> changePathBreaker(editor, CycleUtils::tagPathBreakerSelfloopPins));

        JButton tagPathBreakerAutoAppendButton = GuiUtils.createIconButton(
                "images/circuit-cycle-auto_append.svg",
                "Auto-append path breaker pins as necessary to complete cycle breaking",
                l -> changePathBreaker(editor, CycleUtils::tagPathBreakerAutoAppend));

        JButton tagPathBreakerAutoDiscardButton = GuiUtils.createIconButton(
                "images/circuit-cycle-auto_discard.svg",
                "Auto-discard path breaker pins that are redundant for cycle breaking",
                l -> changePathBreaker(editor, CycleUtils::tagPathBreakerAutoDiscard));

        JButton tagPathBreakerClearAllButton = GuiUtils.createIconButton(
                "images/circuit-cycle-clear_all.svg",
                "Clear all path breaker pins",
                l -> changePathBreaker(editor, CycleUtils::tagPathBreakerClearAll));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(tagPathBreakerSelfloopPinsButton);
        buttonPanel.add(tagPathBreakerAutoAppendButton);
        buttonPanel.add(tagPathBreakerAutoDiscardButton);
        buttonPanel.add(tagPathBreakerClearAllButton);
        GuiUtils.setButtonPanelLayout(buttonPanel, tagPathBreakerClearAllButton.getPreferredSize());

        JPanel controlPanel = new JPanel(new WrapLayout());
        controlPanel.add(buttonPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(driverPinTable), new JScrollPane(drivenPinTable));

        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.add(controlPanel, BorderLayout.NORTH);
        forcePanel.add(splitPane, BorderLayout.CENTER);
        return forcePanel;
    }

    private void changePathBreaker(final GraphEditor editor, Function<Circuit, Collection<? extends Contact>> func) {
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

    private JPanel getScanControlsPanel(final GraphEditor editor) {
        JButton insertTestableGatesButton = new JButton("<html><center>Insert<br><small>TBUF/TINV</small></center></html>");
        insertTestableGatesButton.addActionListener(l -> insertTestableGates(editor));
        insertTestableGatesButton.setToolTipText("Insert testable buffers/inverters for path breaker components");

        JButton insertScanButton = new JButton("<html><center>Insert<br><small>SCAN</small></center></html>");
        insertScanButton.addActionListener(l -> insertScan(editor));
        insertScanButton.setToolTipText("Insert scan for path breaker components");

        writeConstraintsButton = new JButton("<html><center>Write<br><small>SDC...</small></center></html>");
        writeConstraintsButton.addActionListener(l -> writeConstraints(editor));
        writeConstraintsButton.setToolTipText("<html>Write <i>set_disable_timing</i> constraints for <b>input pin</b> path breakers</html>");

        JPanel scanPanel = new JPanel(new WrapLayout());
        scanPanel.add(insertTestableGatesButton);
        scanPanel.add(insertScanButton);
        scanPanel.add(writeConstraintsButton);
        return scanPanel;
    }

    private void insertTestableGates(GraphEditor editor) {
        VisualCircuit circuit = (VisualCircuit) editor.getModel();
        editor.getWorkspaceEntry().saveMemento();
        ScanUtils.insertTestableGates(circuit);
        editor.requestFocus();
    }

    private void insertScan(GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        we.captureMemento();
        boolean isModified = ScanUtils.insertScan(circuit);
        if (isModified) {
            we.saveMemento();
        }
        we.uncaptureMemento();
        editor.requestFocus();
    }

    private void writeConstraints(final GraphEditor editor) {
        File file = new File(editor.getWorkspaceEntry().getFileName());
        Format format = SdcFormat.getInstance();
        JFileChooser fc = DialogUtils.createFileSaver("Save path breaker SDC constraints", file, format);
        try {
            file = DialogUtils.chooseValidSaveFileOrCancel(fc, format);
            Circuit circuit = WorkspaceUtils.getAs(editor.getWorkspaceEntry(), Circuit.class);
            LogUtils.logInfo(ExportUtils.getExportMessage(circuit, file, "Writing path breaker constraints for the circuit "));
            try (FileOutputStream out = new FileOutputStream(file)) {
                PathbreakSerialiserUtils.write(circuit, out);
            } catch (IOException e) {
                LogUtils.logError("Could not write into file '" + file.getAbsolutePath() + "'");
            }
            Framework.getInstance().setLastDirectory(fc.getCurrentDirectory());
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }

    @Override
    public String getLabel() {
        return "Cycle analyser";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_Y;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/circuit-tool-cycle_analysis.svg");
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a contact or a gate to toggle its path breaker state.";
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
        cycleState = null;
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
        cycleState = new CycleState(circuit);
        driverPinTable.refresh();
        drivenPinTable.refresh();
        // Enable write SDC constraints button if there are path breaker input pins
        boolean hasDrivenPathBreaker = !cycleState.getBreakerDrivenPins().isEmpty();
        writeConstraintsButton.setEnabled(hasDrivenPathBreaker);
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
            if (deepestNode instanceof VisualContact) {
                contact = (VisualContact) deepestNode;
            } else if (deepestNode instanceof VisualCircuitComponent) {
                VisualFunctionComponent component = (VisualFunctionComponent) deepestNode;
                contact = component.getMainVisualOutput();

            }
            if ((contact != null) && contact.isPin() && !contact.isZeroDelayDriver()) {
                FunctionContact mathContact = ((VisualFunctionContact) contact).getReferencedComponent();
                editor.getWorkspaceEntry().saveMemento();
                mathContact.setPathBreaker(!mathContact.getPathBreaker());
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
            Node mathNode = null;
            if (node instanceof VisualComponent) {
                mathNode = ((VisualComponent) node).getReferencedComponent();
            }

            if (mathNode != null) {
                if (mathNode instanceof Contact) {
                    return getContactDecoration((Contact) mathNode);
                }
                if (mathNode instanceof FunctionComponent) {
                    return getComponentDecoration((FunctionComponent) mathNode);
                }
            }
            return null;
        };
    }

    private Color getCycleStatusColor(Contact contact) {
        return contact.isZeroDelayDriver() ? AnalysisDecorationSettings.getDontTouchColor()
                : cycleState.isInCycle(contact) ? AnalysisDecorationSettings.getProblemColor()
                : contact.getPathBreaker() ? AnalysisDecorationSettings.getFixerColor()
                : contact.isPin() ? AnalysisDecorationSettings.getClearColor() : null;
    }

    private Decoration getContactDecoration(Contact contact) {
        final Color color = getCycleStatusColor(contact);

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

    private Decoration getComponentDecoration(FunctionComponent component) {
        final Color color = component.getIsZeroDelay() ? AnalysisDecorationSettings.getDontTouchColor()
                : cycleState.isInCycle(component) ? AnalysisDecorationSettings.getProblemColor()
                : ScanUtils.hasPathBreakerOutput(component) ? AnalysisDecorationSettings.getFixerColor()
                : AnalysisDecorationSettings.getClearColor();

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

    private class DriverPinTable extends JTable {

        DriverPinTable() {
            setModel(new DriverPinTableModel());
            setFocusable(false);
            setRowSelectionAllowed(false);
            setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            getTableHeader().setDefaultRenderer(new FlatHeaderRenderer(false));
            getTableHeader().setReorderingAllowed(false);
            setDefaultRenderer(Object.class, new PinTableCellRenderer());
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Contact contact = cycleState.getDriverPin(getSelectedRow());
                    if (contact != null) {
                        contact.setPathBreaker(!contact.getPathBreaker());
                    }
                }
            });
        }

        public void refresh() {
            int cyclePinCount = cycleState.getCycleDriverPins().size();
            int breakerPinCount = cycleState.getBreakerDriverPins().size();
            int clearPinCount = cycleState.getDriverPinCount() - cyclePinCount - breakerPinCount;
            String header = StatsUtils.getHtmlStatsHeader("Output pins",
                    cyclePinCount, null, breakerPinCount, clearPinCount);

            GuiUtils.setColumnHeader(this, 0, header);

            final Color color = !cycleState.getCycleDriverPins().isEmpty() ? AnalysisDecorationSettings.getProblemColor()
                    : !cycleState.getBreakerDriverPins().isEmpty() ? AnalysisDecorationSettings.getFixerColor()
                    : AnalysisDecorationSettings.getClearColor();

            getTableHeader().setBackground(color);
            GuiUtils.refreshTable(this);
        }
    }

    private final class DriverPinTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return cycleState.getDriverPinCount();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return cycleState.getDriverPin(row);
        }
    }

    private class DrivenPinTable extends JTable {

        DrivenPinTable() {
            setModel(new DrivenPinTableModel());
            setFocusable(false);
            setRowSelectionAllowed(false);
            setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setDefaultRenderer(new FlatHeaderRenderer(false));
            setDefaultRenderer(Object.class, new PinTableCellRenderer());
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Contact contact = cycleState.getDrivenPin(getSelectedRow());
                    if (contact != null) {
                        contact.setPathBreaker(!contact.getPathBreaker());
                    }
                }
            });
        }

        public void refresh() {
            int cyclePinCount = cycleState.getCycleDrivenPins().size();
            int breakerPinCount = cycleState.getBreakerDrivenPins().size();
            int clearPinCount = cycleState.getDrivenPinCount() - cyclePinCount - breakerPinCount;
            String header = StatsUtils.getHtmlStatsHeader("Input pins",
                    cyclePinCount, null, breakerPinCount, clearPinCount);

            GuiUtils.setColumnHeader(this, 0, header);

            final Color color = !cycleState.getCycleDrivenPins().isEmpty() ? AnalysisDecorationSettings.getProblemColor()
                    : !cycleState.getBreakerDrivenPins().isEmpty() ? AnalysisDecorationSettings.getFixerColor()
                    : AnalysisDecorationSettings.getClearColor();

            getTableHeader().setBackground(color);
            GuiUtils.refreshTable(this);
        }
    }

    private final class DrivenPinTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return cycleState.getDrivenPinCount();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return cycleState.getDrivenPin(row);
        }
    }

    private final class PinTableCellRenderer implements TableCellRenderer {

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
            if (value instanceof FunctionContact contact) {
                Color color = ColorUtils.colorise(GuiUtils.getTableCellBackgroundColor(), getCycleStatusColor(contact));
                label.setBackground(color);

                String ref = cycleState.getContactReference(contact);
                String text = cycleState.isRedundantPathBreaker(contact) ? WARNING_PREFIX + ref : ref;
                label.setText(text);

                boolean fits = GuiUtils.getLabelTextWidth(label) < GuiUtils.getTableColumnTextWidth(table, col);
                label.setToolTipText(fits ? null : label.getText());
                label.setBorder(GuiUtils.getTableCellBorder());
                result = label;
            }
            return result;
        }
    }

}
