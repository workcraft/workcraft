package org.workcraft.plugins.circuit.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.swing.Icon;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractGraphEditorTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;

public class InitialisationAnalyserTool extends AbstractGraphEditorTool {

    private HashSet<Node> initHighSet;
    private HashSet<Node> initLowSet;
    private HashSet<Node> initErrorSet;

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
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        updateState(circuit);
        super.activated(editor);
    }

    @Override
    public void setup(final GraphEditor editor) {
        super.setup(editor);
        editor.getWorkspaceEntry().setCanModify(false);
        editor.getWorkspaceEntry().setCanSelect(false);
        editor.getWorkspaceEntry().setCanCopy(false);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        initHighSet = null;
        initLowSet = null;
        initErrorSet = null;
    }

    @Override
    public boolean requiresPropertyEditor() {
        return true;
    }

    private void updateState(Circuit circuit) {
        initHighSet = new HashSet<>();
        initLowSet = new HashSet<>();
        initErrorSet = new HashSet<>();
        Queue<Connection> queue = new LinkedList<>();
        for (FunctionContact contact: circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getForcedInit()) {
                HashSet<Node> init = (contact.getInitToOne()) ? initHighSet : initLowSet;
                if (init.add(contact)) {
                    queueConnections(circuit, contact, queue);
                }
            }
        }
        while (!queue.isEmpty()) {
            Connection connection = queue.remove();
            Node fromNode = connection.getFirst();
            HashSet<Node> nodeInitLevelSet = chooseNodeLevelSet(fromNode);
            if ((nodeInitLevelSet != null) && nodeInitLevelSet.add(connection)) {
                if (initErrorSet.contains(fromNode)) {
                    initErrorSet.add(connection);
                }
                Node toNode = connection.getSecond();
                if (nodeInitLevelSet.add(toNode)) {
                    Node parent = toNode.getParent();
                    if (parent instanceof FunctionComponent) {
                        FunctionComponent component = (FunctionComponent) parent;
                        propagateValuesToOutputs(circuit, component, queue);
                    } else {
                        queueConnections(circuit, toNode, queue);
                    }
                }
            }
        }
    }

    private void fillVariableValues(FunctionComponent component,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {
        for (FunctionContact contact: component.getFunctionContacts()) {
            HashSet<Node> contactInitLevelSet = chooseNodeLevelSet(contact);
            if (contactInitLevelSet != null) {
                variables.add(contact);
                values.add(contactInitLevelSet == initHighSet ? One.instance() : Zero.instance());
            }
        }
    }

    private void propagateValuesToOutputs(Circuit circuit, FunctionComponent component, Queue<Connection> queue) {
        boolean progress = true;
        while (progress) {
            progress = false;
            LinkedList<BooleanVariable> variables = new LinkedList<>();
            LinkedList<BooleanFormula> values = new LinkedList<>();
            fillVariableValues(component, variables, values);
            for (FunctionContact outputPin: component.getFunctionOutputs()) {
                Set<Node> outputInitLevelSet = chooseFunctionLevelSet(outputPin, variables, values);
                if ((outputInitLevelSet != null) && outputInitLevelSet.add(outputPin)) {
                    progress = true;
                    if ((outputInitLevelSet == initHighSet) != outputPin.getInitToOne()) {
                        initErrorSet.add(outputPin);
                    }
                    queueConnections(circuit, outputPin, queue);
                }
            }
        }
    }

    private void queueConnections(Circuit circuit, Node node, Queue<Connection> queue) {
        Set<Connection> connections = circuit.getConnections(node);
        queue.addAll(connections);
    }

    private HashSet<Node> chooseNodeLevelSet(Node node) {
        if (initHighSet.contains(node)) {
            return initHighSet;
        }
        if (initLowSet.contains(node)) {
            return initLowSet;
        }
        return null;
    }

    private HashSet<Node> chooseFunctionLevelSet(FunctionContact contact,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {
        BooleanFormula setFunction = BooleanUtils.cleverReplace(contact.getSetFunction(), variables, values);
        BooleanFormula resetFunction = BooleanUtils.cleverReplace(contact.getResetFunction(), variables, values);
        if (isEvaluatedHigh(setFunction, resetFunction)) {
            return initHighSet;
        } else if (isEvaluatedLow(setFunction, resetFunction)) {
            return initLowSet;
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
    public void mousePressed(GraphEditorMouseEvent e) {
        boolean processed = false;
        GraphEditor editor = e.getEditor();
        VisualModel model = e.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            Node node = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    new Func<Node, Boolean>() {
                        @Override
                        public Boolean eval(Node node) {
                            return (node instanceof VisualFunctionComponent) || (node instanceof VisualContact);
                        }
                    });

            VisualContact contact = null;
            if (node instanceof VisualFunctionContact) {
                contact = (VisualFunctionContact) node;
            } else if (node instanceof VisualFunctionComponent) {
                VisualFunctionComponent component = (VisualFunctionComponent) node;
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
                    if ((initHighSet != null) && initHighSet.contains(mathNode)) {
                        return getHighLevelDecoration(mathNode);
                    }
                    if ((initLowSet != null) && initLowSet.contains(mathNode)) {
                        return getLowLevelDecoration(mathNode);
                    }
                }
                return (mathNode instanceof Contact) ? StateDecoration.Empty.INSTANCE : null;
            }
        };
    }

    private Decoration getComponentDecoration(FunctionComponent component) {
        boolean initialised = true;
        boolean initialisationConflict = false;
        for (Contact outputContact: component.getOutputs()) {
            initialised &= ((initHighSet != null) && initHighSet.contains(outputContact))
                    || ((initLowSet != null) && initLowSet.contains(outputContact));
            initialisationConflict |= (initErrorSet != null) && initErrorSet.contains(outputContact);
        }
        final Color color = initialisationConflict ? CircuitSettings.getConflictGateColor()
                : initialised ? CircuitSettings.getInitialisedGateColor() : null;

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
        final boolean initialisationConflict = (initErrorSet != null) && initErrorSet.contains(node);
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
        final boolean initialisationConflict = (initErrorSet != null) && initErrorSet.contains(node);
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
