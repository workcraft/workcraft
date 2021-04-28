package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.OperationCancelledException;
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
import org.workcraft.utils.DialogUtils;
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

    private final BasicTable<String> breakerTable = new BasicTable<>("Path breakers");
    private Set<Contact> cycleContacts;
    private Set<FunctionComponent> cycleComponents;
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
        ScanUtils.insertScan(we);
        editor.requestFocus();
    }

    private void writeConstraints(final GraphEditor editor) {
        File file = new File(editor.getWorkspaceEntry().getFileName());
        PathbreakConstraintExporter exporter = new PathbreakConstraintExporter();
        Format format = exporter.getFormat();
        JFileChooser fc = DialogUtils.createFileSaver("Save path breaker SDC constraints", file, format);
        try {
            file = DialogUtils.chooseValidSaveFileOrCancel(fc, format);
            Circuit circuit = WorkspaceUtils.getAs(editor.getWorkspaceEntry(), Circuit.class);
            exporter.export(circuit, file);
            Framework.getInstance().setLastDirectory(fc.getCurrentDirectory());
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
        editor.getWorkspaceEntry().addObserver(e -> updateState(editor));
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
        we.setCanModify(true);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    private void updateState(final GraphEditor editor) {
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        cycleContacts = CycleUtils.getCycledDrivers(circuit);
        // Add components to "cycle" set if they have pins on a cycle
        cycleComponents = new HashSet<>();
        for (Contact contact : cycleContacts) {
            Node parent = contact.getParent();
            if (parent instanceof FunctionComponent) {
                cycleComponents.add((FunctionComponent) parent);
            }
        }
        // Add zero delay gates and their pins to "cycle" sets they are between components on a cycle
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsZeroDelay()) {
                boolean inputOnCycle = false;
                boolean outputOnCycle = false;
                for (Contact input : component.getInputs()) {
                    Contact driver = CycleUtils.findUnbrokenPathDriverPin(circuit, input);
                    if (driver != null) {
                        inputOnCycle |= cycleComponents.contains(driver.getParent());
                    }
                }
                for (Contact output : component.getOutputs()) {
                    for (Contact driven : CycleUtils.findUnbrokenPathDrivenPins(circuit, output)) {
                        outputOnCycle |= cycleComponents.contains(driven.getParent());
                    }
                }
                if (inputOnCycle && outputOnCycle) {
                    cycleComponents.add(component);
                    cycleContacts.addAll(component.getContacts());
                }
            }
        }
        // Extend the set of cycled pins by input pins in cycled components that are driven by other cycled pins
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (Contact contact : component.getInputs()) {
                if (!contact.getPathBreaker() && cycleComponents.contains(component)) {
                    Contact driver = CycleUtils.findUnbrokenPathDriverPin(circuit, contact);
                    if (cycleContacts.contains(driver)) {
                        cycleContacts.add(contact);
                    }
                }
            }
        }
        // Populate path breaker table and check if there are input pins path breaker
        boolean hasInputPinPathBreaker = false;
        List<String> breakers = new ArrayList<>();
        for (Contact contact : circuit.getFunctionContacts()) {
            if (contact.getPathBreaker()) {
                breakers.add(circuit.getNodeReference(contact));
                hasInputPinPathBreaker |= contact.isInput();
            }
        }
        Collections.sort(breakers);
        breakerTable.set(breakers);
        // Enable write SDC constraints button if there are path breaker input pins
        writeConstraintsButton.setEnabled(hasInputPinPathBreaker);
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
            return (mathNode instanceof Contact) ? StateDecoration.Empty.INSTANCE : null;
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
