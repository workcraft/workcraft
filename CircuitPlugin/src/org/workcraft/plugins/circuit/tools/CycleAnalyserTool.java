package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.tools.*;
import org.workcraft.interop.Format;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
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
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class CycleAnalyserTool extends AbstractGraphEditorTool {

    private final BasicTable<String> breakerTable = new BasicTable("<html><b>Path breakers</b></html>");
    private Set<Contact> cycleContacts;
    private Set<FunctionComponent> cycleComponents;

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
        ColorLegendTable colorLegendTable = new ColorLegendTable(Arrays.asList(
                Pair.of(AnalysisDecorationSettings.getDontTouchColor(), "Don't touch zero delay"),
                Pair.of(AnalysisDecorationSettings.getProblemColor(), "On a cycle"),
                Pair.of(AnalysisDecorationSettings.getFixerColor(), "Path breaker"),
                Pair.of(AnalysisDecorationSettings.getClearColor(), "Not on any cycle")
        ));

        JPanel legendPanel = new JPanel(new BorderLayout());
        legendPanel.setBorder(SizeHelper.getTitledBorder("<html><b>Highlight legend</b></html>"));
        legendPanel.add(colorLegendTable, BorderLayout.CENTER);
        return legendPanel;
    }

    private JPanel getBreakControlsPanel(final GraphEditor editor) {

        JButton tagPathBreakerSelfloopPinsButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-selfloop_pins.svg"),
                "Path breaker all self-loops");
        tagPathBreakerSelfloopPinsButton.addActionListener(l -> changePathBreaker(editor, c -> CycleUtils.tagPathBreakerSelfloopPins(c)));

        JButton tagPathBreakerAutoAppendButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-auto_append.svg"),
                "Auto-append path breaker pins as necessary to complete cycle breaking");
        tagPathBreakerAutoAppendButton.addActionListener(l -> changePathBreaker(editor, c -> CycleUtils.tagPathBreakerAutoAppend(c)));

        JButton tagPathBreakerAutoDiscardButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-auto_discard.svg"),
                "Auto-discard path breaker pins that are redundant for cycle breaking");
        tagPathBreakerAutoDiscardButton.addActionListener(l -> changePathBreaker(editor, c -> CycleUtils.tagPathBreakerAutoDiscard(c)));

        JButton tagPathBreakerClearAllButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/circuit-cycle-clear_all.svg"),
                "Clear all path breaker pins");
        tagPathBreakerClearAllButton.addActionListener(l -> changePathBreaker(editor, c -> CycleUtils.tagPathBreakerClearAll(c)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(tagPathBreakerSelfloopPinsButton);
        buttonPanel.add(tagPathBreakerAutoAppendButton);
        buttonPanel.add(tagPathBreakerAutoDiscardButton);
        buttonPanel.add(tagPathBreakerClearAllButton);
        GuiUtils.setButtonPanelLayout(buttonPanel, tagPathBreakerClearAllButton.getPreferredSize());

        JPanel controlPanel = new JPanel(new WrapLayout());
        controlPanel.add(buttonPanel);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.add(controlPanel, BorderLayout.NORTH);
        forcePanel.add(new JScrollPane(breakerTable), BorderLayout.CENTER);
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
        updateState(editor);
        editor.requestFocus();
    }

    private JPanel getScanControlsPanel(final GraphEditor editor) {
        JButton insertTestableGatesButton = new JButton("<html><center>Insert<br><small>TBUF/TINV</small></center></html>");
        insertTestableGatesButton.addActionListener(l -> insertTestableGates(editor));
        insertTestableGatesButton.setToolTipText("Insert testable buffers/inverters for all path breaker components");

        JButton insertScanButton = new JButton("<html><center>Insert<br><small>SCAN</small></center></html>");
        insertScanButton.addActionListener(l -> insertScan(editor));
        insertScanButton.setToolTipText("Insert scan for all path breaker components");

        JButton writeConstraintsButton = new JButton("<html><center>Write<br><small>SDC...</small></center></html>");
        writeConstraintsButton.addActionListener(l -> writeConstraints(editor));
        writeConstraintsButton.setToolTipText("Write set_disable_timing constraints for path breaker input pins");

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
        updateState(editor);
        editor.requestFocus();
    }

    private void insertScan(GraphEditor editor) {
        VisualCircuit circuit = (VisualCircuit) editor.getModel();
        editor.getWorkspaceEntry().saveMemento();
        ScanUtils.insertScan(circuit);
        updateState(editor);
        editor.requestFocus();
    }

    private void writeConstraints(final GraphEditor editor) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        File file = new File(editor.getWorkspaceEntry().getFileName());
        PathbreakConstraintExporter exporter = new PathbreakConstraintExporter();
        Format format = exporter.getFormat();
        JFileChooser fc = mainWindow.createSaveDialog("Save path breaker SDC constraints", file, format);
        try {
            String path = ExportUtils.getValidSavePath(fc, format);
            Circuit circuit = WorkspaceUtils.getAs(editor.getWorkspaceEntry(), Circuit.class);
            exporter.export(circuit, new File(path));
            framework.setLastDirectory(fc.getCurrentDirectory());
        } catch (OperationCancelledException e) {
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
        CircuitUtils.correctZeroDelayInitialState(circuit);
        updateState(editor);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        cycleContacts = null;
        cycleComponents = null;
        breakerTable.clear();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    private void updateState(final GraphEditor editor) {
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        cycleContacts = CycleUtils.getCycledDrivers(circuit);
        cycleComponents = new HashSet<>();
        for (Contact contact : cycleContacts) {
            Node parent = contact.getParent();
            if (parent instanceof FunctionComponent) {
                cycleComponents.add((FunctionComponent) parent);
            }
        }
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsZeroDelay()) {
                boolean inputOnCycle = false;
                boolean outputOnCycle = false;
                for (Contact contact : component.getInputs()) {
                    Contact driver = CircuitUtils.findDriver(circuit, contact, true);
                    inputOnCycle |= cycleComponents.contains(driver.getParent());
                }
                for (Contact contact : component.getOutputs()) {
                    for (Contact driven : CircuitUtils.findDriven(circuit, contact, true)) {
                        outputOnCycle |= cycleComponents.contains(driven.getParent());
                    }
                }
                if (inputOnCycle && outputOnCycle) {
                    cycleComponents.add(component);
                }
            }
        }
        List<String> breakers = new ArrayList<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (Contact contact : component.getContacts()) {
                if (contact.getPathBreaker()) {
                    breakers.add(circuit.getNodeReference(contact));
                } else if (contact.isInput() && cycleComponents.contains(component)) {
                    Contact driver = CircuitUtils.findDriver(circuit, contact, true);
                    if (cycleContacts.contains(driver)) {
                        cycleContacts.add(contact);
                    }
                }
            }
        }
        Collections.sort(breakers);
        breakerTable.set(breakers);
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
                FunctionContact mathContact = ((VisualFunctionContact) contact).getReferencedContact();
                editor.getWorkspaceEntry().saveMemento();
                mathContact.setPathBreaker(!mathContact.getPathBreaker());
                processed = true;
            }
        }
        if (processed) {
            updateState(editor);
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
        final Color color = contact.isZeroDelayDriver() ? AnalysisDecorationSettings.getDontTouchColor()
                : cycleContacts.contains(contact) ? AnalysisDecorationSettings.getProblemColor()
                : contact.getPathBreaker() ? AnalysisDecorationSettings.getFixerColor()
                : contact.isPin() ? AnalysisDecorationSettings.getClearColor() : null;

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
                : cycleComponents.contains(component) ? AnalysisDecorationSettings.getProblemColor()
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

}
