package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.InitialisationState;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class InitialisationAnalyserTool extends AbstractGraphEditorTool {

    private final BasicTable<String> forcedTable = new BasicTable<>("Force init pins");
    private InitialisationState initState = null;

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
                Pair.of(VisualCommonSettings.getFillColor(), "Unknown initial state"),
                Pair.of(AnalysisDecorationSettings.getDontTouchColor(), "Don't touch zero delay"),
                Pair.of(AnalysisDecorationSettings.getProblemColor(), "Problem of initialisation"),
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
        return highKey + " / " + lowKey + " " + description;
    }

    private JPanel getForcedControlsPanel(final GraphEditor editor) {
        JButton tagForceInitInputPortsButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-input_ports.svg",
                "Force init all input ports (environment responsibility)",
                l -> changeForceInit(editor, ResetUtils::tagForceInitInputPorts));

        JButton tagForceInitNecessaryPinsButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-problematic_pins.svg",
                "Force init output pins with problematic initial state",
                l -> changeForceInit(editor, ResetUtils::tagForceInitProblematicPins));

        JButton tagForceInitSequentialPinsButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-sequential_pins.svg",
                "Force init output pins of sequential gates",
                l -> changeForceInit(editor, ResetUtils::tagForceInitSequentialPins));

        JButton tagForceInitAutoAppendButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-auto_append.svg",
                "Auto-append force init pins as necessary to complete initialisation",
                l -> changeForceInit(editor, ResetUtils::tagForceInitAutoAppend));

        JButton tagForceInitAutoDiscardButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-auto_discard.svg",
                "Auto-discard force init pins that are redundant for initialisation",
                l -> changeForceInit(editor, ResetUtils::tagForceInitAutoDiscard));

        JButton tagForceInitClearAllButton = GuiUtils.createIconButton(
                "images/circuit-initialisation-clear_all.svg",
                "Clear all force init ports and pins",
                l -> changeForceInit(editor, ResetUtils::tagForceInitClearAll));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(tagForceInitInputPortsButton);
        buttonPanel.add(tagForceInitNecessaryPinsButton);
        buttonPanel.add(tagForceInitSequentialPinsButton);
        buttonPanel.add(tagForceInitAutoAppendButton);
        buttonPanel.add(tagForceInitAutoDiscardButton);
        buttonPanel.add(tagForceInitClearAllButton);
        GuiUtils.setButtonPanelLayout(buttonPanel, tagForceInitInputPortsButton.getPreferredSize());

        JPanel controlPanel = new JPanel(new WrapLayout());
        controlPanel.add(buttonPanel);

        JPanel forcePanel = new JPanel(new BorderLayout());
        forcePanel.add(controlPanel, BorderLayout.NORTH);
        forcePanel.add(new JScrollPane(forcedTable), BorderLayout.CENTER);
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
        ResetUtils.insertReset(editor.getWorkspaceEntry(), isActiveLow);
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
        CircuitUtils.correctZeroDelayInitialState(circuit);
        updateState(editor);
        editor.getWorkspaceEntry().addObserver(e -> updateState(editor));
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        forcedTable.clear();
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
        List<String> forcedPins = new ArrayList<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getForcedInit()) {
                String pinRef = circuit.getNodeReference(contact);
                forcedPins.add(pinRef);
            }
        }
        Collections.sort(forcedPins);
        forcedTable.set(forcedPins);
        initState = new InitialisationState(circuit);
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

}
