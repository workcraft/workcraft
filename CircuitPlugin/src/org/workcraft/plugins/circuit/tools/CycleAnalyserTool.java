package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.*;
import org.workcraft.interop.Format;
import org.workcraft.interop.FormatFileFilter;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.serialisation.PathbreakConstraintExporter;
import org.workcraft.plugins.circuit.utils.CycleUtils;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.plugins.circuit.utils.StructureUtilsKt;
import org.workcraft.types.Pair;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.Hierarchy;
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
import java.util.Queue;
import java.util.*;

public class CycleAnalyserTool extends AbstractGraphEditorTool {

    private JTable breakTable;
    private HashSet<MathNode> cycleSet;
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
                Pair.of(CircuitSettings.getWithinCycleGateColor(), "Within cycle"),
                Pair.of(CircuitSettings.getBreakCycleGateColor(), "Path breaker"),
                Pair.of(CircuitSettings.getOutsideCycleGateColor(), "Outside cycle")
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
                GuiUtils.createIconFromSVG("images/circuit-pathbreaker-clear_all.svg"),
                "Clear path breaker contacts and components");
        clearPathBreakersButton.addActionListener(l -> clearPathBreakers(editor));

        JButton insertCycleBreakerBuffersButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-pathbreaker-insert_buffers.svg"),
                "Insert cycle breaker buffers");
        insertCycleBreakerBuffersButton.addActionListener(l -> insertCycleBreakerBuffers(editor));

        FlowLayout flowLayout = new FlowLayout();
        int buttonWidth = (int) Math.round(insertCycleBreakerBuffersButton.getPreferredSize().getWidth() + flowLayout.getHgap());
        int buttonHeight = (int) Math.round(insertCycleBreakerBuffersButton.getPreferredSize().getHeight() + flowLayout.getVgap());
        Dimension panelSize = new Dimension(buttonWidth * 2 + flowLayout.getHgap(), buttonHeight + flowLayout.getVgap());

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(flowLayout);
        btnPanel.setPreferredSize(panelSize);
        btnPanel.setMaximumSize(panelSize);
        btnPanel.add(clearPathBreakersButton);
        btnPanel.add(insertCycleBreakerBuffersButton);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.setBorder(SizeHelper.getTitledBorder("Path breakers"));
        forcePanel.add(forceScrollPane, BorderLayout.CENTER);
        forcePanel.add(btnPanel, BorderLayout.SOUTH);
        return forcePanel;
    }

    private void clearPathBreakers(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.captureMemento();
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        Collection<? extends FunctionComponent> changedComponents = CycleUtils.clearPathBreakerComponents(circuit);
        Collection<? extends Contact> changedContacts = CycleUtils.clearPathBreakerContacts(circuit);
        if (changedComponents.isEmpty() && changedContacts.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
        }
        circuit = (Circuit) editor.getModel().getMathModel();
        updateState(circuit);
        editor.requestFocus();
    }

    private void insertCycleBreakerBuffers(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.captureMemento();
        VisualCircuit circuit = (VisualCircuit) editor.getModel();
        Collection<VisualFunctionComponent> gates = CycleUtils.insertCycleBreakerBuffers(circuit);
        if (gates.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
        }
        circuit = (VisualCircuit) editor.getModel();
        updateState(circuit.getMathModel());
        editor.requestFocus();
    }

    private JPanel getScanControlsPanel(final GraphEditor editor) {
        JButton insertScanButton = new JButton("Insert scan");
        insertScanButton.addActionListener(l -> insertScan(editor));

        JButton writePathbreakConstraintsButton = new JButton("Write SDC");
        writePathbreakConstraintsButton.addActionListener(l -> writePathbreakConstraints(editor));

        JPanel scanPanel = new JPanel();
        scanPanel.add(insertScanButton);
        scanPanel.add(writePathbreakConstraintsButton);
        return scanPanel;
    }

    private void insertScan(GraphEditor editor) {
        VisualCircuit circuit = (VisualCircuit) editor.getModel();
        Collection<VisualFunctionComponent> components = Hierarchy.getDescendantsOfType(circuit.getRoot(),
                VisualFunctionComponent.class, component -> component.getReferencedComponent().getPathBreaker());

        if (!components.isEmpty()) {
            editor.getWorkspaceEntry().saveMemento();
            ScanUtils.insertScan(circuit, components);
        }
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
        return KeyEvent.VK_L;
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
        cycleSet = null;
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
        cycleSet = new HashSet<>();
        breakers.clear();
        HashMap<MathNode, HashSet<CircuitComponent>> presets = new HashMap<>();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) {
                breakers.add(circuit.getNodeReference(component));
            } else {
                HashSet<CircuitComponent> componentPreset = new HashSet<>();
                for (Contact contact : component.getInputs()) {
                    if (contact.getPathBreaker()) {
                        breakers.add(circuit.getNodeReference(contact));
                    } else {
                        HashSet<CircuitComponent> contactPreset = StructureUtilsKt.getPresetComponents(circuit, contact);
                        componentPreset.addAll(contactPreset);
                        presets.put(contact, contactPreset);
                    }
                }
                presets.put(component, componentPreset);
            }
        }
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            for (Contact contact: component.getInputs()) {
                HashSet<CircuitComponent> contactPreset = presets.get(contact);
                if (contactPreset == null) continue;
                HashSet<CircuitComponent> visited = new HashSet<>();
                Queue<CircuitComponent> queue = new LinkedList<>(contactPreset);
                while (!queue.isEmpty()) {
                    CircuitComponent predComponent = queue.remove();
                    if (visited.contains(predComponent)) continue;
                    visited.add(predComponent);
                    if (predComponent == component) {
                        cycleSet.add(component);
                        cycleSet.add(contact);
                        break;
                    }
                    HashSet<CircuitComponent> componentPreset = presets.get(predComponent);
                    if (componentPreset != null) {
                        queue.addAll(componentPreset);
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
            VisualNode node = HitMan.hitDeepest(e.getPosition(), editor.getModel());
            if (node instanceof VisualContact) {
                Contact contact = ((VisualContact) node).getReferencedContact();
                if (contact.isDriven()) {
                    editor.getWorkspaceEntry().saveMemento();
                    contact.setPathBreaker(!contact.getPathBreaker());
                    processed = true;
                }
            } else if (node instanceof VisualCircuitComponent) {
                CircuitComponent component = ((VisualCircuitComponent) node).getReferencedComponent();
                editor.getWorkspaceEntry().saveMemento();
                component.setPathBreaker(!component.getPathBreaker());
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
                : cycleSet.contains(contact) ? CircuitSettings.getWithinCycleGateColor()
                : null;

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
        final Color color = component.getPathBreaker() ? CircuitSettings.getBreakCycleGateColor()
                : cycleSet.contains(component) ? CircuitSettings.getWithinCycleGateColor()
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
