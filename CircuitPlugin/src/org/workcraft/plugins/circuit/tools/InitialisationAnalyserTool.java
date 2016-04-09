package org.workcraft.plugins.circuit.tools;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanUtils;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;

public class InitialisationAnalyserTool extends AbstractTool {

    private Circuit circuit;
    private ArrayList<String> signals;
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
        return GUI.createIconFromSVG("images/icons/svg/tool-initialisation_analysis.svg");
    }

    @Override
    public void activated(final GraphEditor editor) {
        circuit = (Circuit) editor.getModel().getMathModel();
        signals = getSignals();
        updateState(circuit);
        super.activated(editor);
    }

    private ArrayList<String> getSignals() {
        ArrayList<String> result = new ArrayList<>();
        if (circuit != null) {
            for (Contact contact: circuit.getFunctionContacts()) {
                if (contact.isDriver()) {
                    String ref = circuit.getNodeReference(contact);
                    result.add(ref);
                }
            }
        }
        return result;
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        signals = null;
        initHighSet = null;
        initLowSet = null;
        initErrorSet = null;
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
                    Set<Connection> connections = circuit.getConnections(contact);
                    queue.addAll(connections);
                }
            }
        }
        while (!queue.isEmpty()) {
            Connection connection = queue.remove();
            Node fromNode = connection.getFirst();
            HashSet<Node> nodeInitLevelSet = chooseNodeLevelSet(fromNode, initHighSet, initLowSet);
            if ((nodeInitLevelSet != null) && nodeInitLevelSet.add(connection)) {
                if (initErrorSet.contains(fromNode)) {
                    initErrorSet.add(connection);
                }
                Node toNode = connection.getSecond();
                if (nodeInitLevelSet.add(toNode)) {
                    Node parent = toNode.getParent();
                    if (parent instanceof FunctionComponent) {
                        LinkedList<BooleanVariable> variables = new LinkedList<>();
                        LinkedList<BooleanFormula> values = new LinkedList<>();
                        LinkedList<FunctionContact> outputPins = new LinkedList<>();
                        for (FunctionContact contact: Hierarchy.getChildrenOfType(parent, FunctionContact.class)) {
                            if (contact.isOutput()) {
                                outputPins.add(contact);
                            }
                            HashSet<Node> contactInitLevelSet = chooseNodeLevelSet(contact, initHighSet, initLowSet);
                            if (contactInitLevelSet != null) {
                                variables.add(contact);
                                values.add(contactInitLevelSet == initHighSet ? One.instance() : Zero.instance());
                            }
                        }
                        for (FunctionContact outputPin: outputPins) {
                            Set<Node> outputInitLevelSet = chooseFunctionLevelSet(outputPin, variables, values, initHighSet, initLowSet);
                            if ((outputInitLevelSet != null) && outputInitLevelSet.add(outputPin)) {
                                if ((outputInitLevelSet == initHighSet) != outputPin.getInitToOne()) {
                                    initErrorSet.add(outputPin);
                                }
                                Set<Connection> connections = circuit.getConnections(outputPin);
                                queue.addAll(connections);
                            }
                        }
                    } else {
                        Set<Connection> connections = circuit.getConnections(toNode);
                        queue.addAll(connections);
                    }
                }
            }
        }
    }

    private HashSet<Node> chooseNodeLevelSet(Node node, HashSet<Node> highSet, HashSet<Node> lowSet) {
        if (highSet.contains(node)) {
            return highSet;
        }
        if (lowSet.contains(node)) {
            return lowSet;
        }
        return null;
    }

    private HashSet<Node> chooseFunctionLevelSet(FunctionContact contact, LinkedList<BooleanVariable> variables,
            LinkedList<BooleanFormula> values, HashSet<Node> highSet, HashSet<Node> lowSet) {
        BooleanFormula setFunction = BooleanUtils.cleverReplace(contact.getSetFunction(), variables, values);
        BooleanFormula resetFunction = BooleanUtils.cleverReplace(contact.getResetFunction(), variables, values);
        if (isEvaluatedHigh(setFunction, resetFunction)) {
            return highSet;
        } else if (isEvaluatedLow(setFunction, resetFunction)) {
            return lowSet;
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
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
            if (node instanceof VisualContact) {
                Contact contact = ((VisualContact) node).getReferencedContact();
                if (contact.isDriver()) {
                    editor.getWorkspaceEntry().saveMemento();
                    contact.setForcedInit(!contact.getForcedInit());
                    Circuit circuit = (Circuit) editor.getModel().getMathModel();
                    updateState(circuit);
                    processed = true;
                }
            }
        }

        if (!processed) {
            super.mouseClicked(e);
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

    @Override
    public String getHintMessage() {
        return "Click on a driver contact to toggle its force initialisation state.";
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
