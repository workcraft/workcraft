package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.*;
import org.workcraft.interop.Format;
import org.workcraft.interop.FormatFileFilter;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.serialisation.PathbreakConstraintExporter;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.CycleUtils;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class CycleAnalyserTool extends AbstractGraphEditorTool {

    private JTable breakTable;
    private Set<Contact> cycleContacts;
    private Set<FunctionComponent> cycleComponents;
    private final List<String> breakers = new ArrayList<>();

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getLegendControlsPanel(editor), BorderLayout.NORTH);
        panel.add(getBreakControlsPanel(editor), BorderLayout.CENTER);
        panel.add(getScanControlsPanel(editor), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private JPanel getLegendControlsPanel(final GraphEditor editor) {
        ColorLegendTableModel legendTableModel = new ColorLegendTableModel(Arrays.asList(
                Pair.of(CircuitSettings.getWithinCycleGateColor(), "Within a cycle"),
                Pair.of(CircuitSettings.getBreakCycleGateColor(), "Path breaker"),
                Pair.of(CircuitSettings.getOutsideCycleGateColor(), "Outside of all cycles")
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
        legendPanel.setBorder(SizeHelper.getTitledBorder("Highlight legend"));
        legendPanel.add(legendTable, BorderLayout.CENTER);
        return legendPanel;
    }

    private JPanel getBreakControlsPanel(final GraphEditor editor) {
        BasicTableModel<String> breakTableModel = new BasicTableModel<>(breakers);
        breakTable = new JTable(breakTableModel);
        breakTable.setFocusable(false);
        breakTable.setRowSelectionAllowed(false);
        breakTable.setRowHeight(SizeHelper.getComponentHeightFromFont(breakTable.getFont()));
        breakTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        breakTable.setTableHeader(null);
        JScrollPane forceScrollPane = new JScrollPane(breakTable);

        JButton clearPathBreakersButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-clear_all.svg"),
                "Clear path breaker contacts and components");
        clearPathBreakersButton.addActionListener(l -> changeForceInit(editor, c -> CycleUtils.clearPathBreakers(c)));

        JButton tagNecessaryPathBreakersButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-tag_necessary.svg"),
                "Add path breakers if necessary for cycle breaking");
        tagNecessaryPathBreakersButton.addActionListener(l -> changeForceInit(editor, c -> CycleUtils.tagNecessaryPathBreakers(c)));

        JButton untagRedundantPathBreakersButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-untag_redundant.svg"),
                "Remove path breakers if redundant for cycle breaking");
        untagRedundantPathBreakersButton.addActionListener(l -> changeForceInit(editor, c -> CycleUtils.untagRedundantPathBreakers(c)));

        JButton setSelfLoopsPathBreakersButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-self_loop.svg"),
                "Add path breaker for all self-loops");
        setSelfLoopsPathBreakersButton.addActionListener(l -> changeForceInit(editor, c -> CycleUtils.setSelfLoopPathBreakers(c)));

        FlowLayout flowLayout = new FlowLayout();
        Dimension buttonSize = clearPathBreakersButton.getPreferredSize();
        int buttonWidth = (int) Math.round(buttonSize.getWidth() + flowLayout.getHgap());
        int buttonHeight = (int) Math.round(buttonSize.getHeight() + flowLayout.getVgap());
        Dimension panelSize = new Dimension(buttonWidth * 3 + flowLayout.getHgap(), buttonHeight + flowLayout.getVgap());

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(flowLayout);
        btnPanel.setPreferredSize(panelSize);
        btnPanel.setMaximumSize(panelSize);
        btnPanel.add(setSelfLoopsPathBreakersButton);
        btnPanel.add(tagNecessaryPathBreakersButton);
        btnPanel.add(untagRedundantPathBreakersButton);
        btnPanel.add(clearPathBreakersButton);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.setBorder(SizeHelper.getTitledBorder("Path breakers"));
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

    private JPanel getScanControlsPanel(final GraphEditor editor) {
        JButton insertScanButton = new JButton("Insert scan");
        insertScanButton.addActionListener(l -> insertScan(editor));

        JButton writePathbreakConstraintsButton = new JButton("Write SDC...");
        writePathbreakConstraintsButton.addActionListener(l -> writePathbreakConstraints(editor));

        JPanel scanPanel = new JPanel();
        scanPanel.add(insertScanButton);
        scanPanel.add(writePathbreakConstraintsButton);
        return scanPanel;
    }

    private void insertScan(GraphEditor editor) {
        editor.getWorkspaceEntry().saveMemento();
        VisualCircuit circuit = (VisualCircuit) editor.getModel();
        ScanUtils.insertScan(circuit);
        updateState(((VisualCircuit) editor.getModel()).getMathModel());
    }

    private void writePathbreakConstraints(final GraphEditor editor) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setDialogTitle("Save path breaker SDC constraints");
        Circuit circuit = WorkspaceUtils.getAs(editor.getWorkspaceEntry(), Circuit.class);
        PathbreakConstraintExporter exporter = new PathbreakConstraintExporter();
        Format format = exporter.getFormat();
        fc.setFileFilter(new FormatFileFilter(format));
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        GuiUtils.sizeFileChooserToScreen(fc, mainWindow.getDisplayMode());
        fc.setCurrentDirectory(mainWindow.getLastDirectory());
        try {
            String path = ExportUtils.getValidSavePath(fc, format);
            File file = new File(path);
            exporter.export(circuit, file);
        } catch (OperationCancelledException e) {
        }
        mainWindow.setLastDirectory(fc.getCurrentDirectory());
    }

    @Override
    public String getLabel() {
        return "Cycle analyser";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_A;
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
        return "Click on a driven contact or a component to toggle its path breaker state.";
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
        cycleContacts = null;
        cycleComponents = null;
        breakers.clear();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    private void updateState(Circuit circuit) {
        cycleContacts = CycleUtils.getCycledDrivers(circuit);
        cycleComponents = new HashSet<>();
        for (Contact contact : cycleContacts) {
            Node parent = contact.getParent();
            if (parent instanceof FunctionComponent) {
                cycleComponents.add((FunctionComponent) parent);
            }
        }

        breakers.clear();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (Contact contact : component.getContacts()) {
                if (contact.getPathBreaker()) {
                    breakers.add(circuit.getNodeReference(contact));
                } else if (contact.isInput()) {
                    Contact driver = CircuitUtils.findDriver(circuit, contact, true);
                    if (cycleContacts.contains(driver)) {
                        cycleContacts.add(contact);
                    }
                }
            }
        }
        Collections.sort(breakers);
        breakTable.tableChanged(new TableModelEvent(breakTable.getModel()));
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
            if ((contact != null) && contact.isPin()) {
                FunctionContact mathContact = ((VisualFunctionContact) contact).getReferencedContact();
                editor.getWorkspaceEntry().saveMemento();
                mathContact.setPathBreaker(!mathContact.getPathBreaker());
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
                    if (mathNode instanceof Contact) {
                        return getContactDecoration((Contact) mathNode);
                    }
                    if (mathNode instanceof FunctionComponent) {
                        return getComponentDecoration((FunctionComponent) mathNode);
                    }
                }
                return (mathNode instanceof Contact) ? StateDecoration.Empty.INSTANCE : null;
            }
        };
    }

    private Decoration getContactDecoration(Contact contact) {
        final Color color = contact.getPathBreaker() ? CircuitSettings.getBreakCycleGateColor()
                : cycleContacts.contains(contact) ? CircuitSettings.getWithinCycleGateColor()
                : contact.isPin() ? CircuitSettings.getOutsideCycleGateColor() : null;

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
        final Color color = ScanUtils.hasPathBreakerOutput(component) ? CircuitSettings.getBreakCycleGateColor()
                : cycleComponents.contains(component) ? CircuitSettings.getWithinCycleGateColor()
                : CircuitSettings.getOutsideCycleGateColor();

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

}
