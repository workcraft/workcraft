package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.naryformula.SplitForm;
import org.workcraft.plugins.circuit.naryformula.SplitFormGenerator;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DecomposeTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Decompose gates (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Decompose gate";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return (node instanceof VisualFunctionComponent) && ((VisualFunctionComponent) node).isGate();
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> components = new HashSet<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            components.addAll(Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualFunctionComponent.class));
            Collection<Node> selection = circuit.getSelection();
            if (!selection.isEmpty()) {
                components.retainAll(selection);
            }
        }
        return components;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualFunctionComponent)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualFunctionComponent bigComponent = (VisualFunctionComponent) node;
            if (bigComponent.isGate()) {
                String ref = circuit.getNodeMathReference(bigComponent);
                VisualFunctionContact output = bigComponent.getGateOutput();
                BooleanFormula setFunction = output.getSetFunction();
                BooleanFormula resetFunction = output.getResetFunction();
                if ((setFunction != null) && (resetFunction != null)) {
                    LogUtils.logWarning("Gate '" + ref + "' cannot be decomposed as it has both set and reset functions defined");
                } else if (setFunction == null) {
                    LogUtils.logWarning("Gate '" + ref + "' cannot be decomposed as it does not have set functions defined");
                } else {
                    LogUtils.logInfo("Decomposing gate '" + ref + "': " + FormulaToString.toString(setFunction));
                    SplitForm functions = SplitFormGenerator.generate(setFunction);

                    Collection<Node> fromNodes = new LinkedList<>();
                    for (VisualContact input: bigComponent.getVisualInputs()) {
                        Set<Connection> connections = circuit.getConnections(input);
                        if (connections.isEmpty()) {
                            fromNodes.add(null);
                        } else {
                            for (Connection connection: connections) {
                                fromNodes.add(connection.getFirst());
                            }
                        }
                    }
                    Queue<Collection<Node>> toNodesQueue = new LinkedList<>();
                    Collection<Node> toNodes = new LinkedList<>();
                    for (Connection connection: circuit.getConnections(output)) {
                        toNodes.add(connection.getSecond());
                    }
                    toNodesQueue.add(toNodes);
                    Container container = (Container) bigComponent.getParent();
                    double x = bigComponent.getX();
                    double y = bigComponent.getY();
                    circuit.remove(bigComponent);

                    List<BooleanFormula> clauses = functions.getClauses();
                    x = x + clauses.size();
                    for (BooleanFormula function: clauses) {
                        LogUtils.logMessage(FormulaToString.toString(function));

                        FunctionComponent mathComponent = new FunctionComponent();
                        Container mathContainer = NamespaceHelper.getMathContainer(circuit, container);
                        mathContainer.add(mathComponent);

                        VisualFunctionComponent component = circuit.createVisualComponent(mathComponent, VisualFunctionComponent.class, container);
                        component.setPosition(new Point2D.Double(x, y));
                        x -= 2.0;

                        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, null, IOType.OUTPUT);
                        try {
                            BooleanFormula parseContactFuncton = CircuitUtils.parseContactFuncton(circuit, component, FormulaToString.toString(function));
                            outputContact.setSetFunction(parseContactFuncton);
                        } catch (ParseException e1) {
                        }

                        for (VisualContact inputContact: component.getVisualInputs()) {
                            toNodesQueue.add(Arrays.asList(inputContact));
                        }

                        if (!toNodesQueue.isEmpty()) {
                            toNodes = toNodesQueue.remove();
                            for (Node toNode: toNodes) {
                                try {
                                    circuit.connect(outputContact, toNode);
                                } catch (InvalidConnectionException e) {
                                    LogUtils.logWarning(e.getMessage());
                                }
                            }
                        }
                    }
                    for (Node fromNode: fromNodes) {
                        if (!toNodesQueue.isEmpty()) {
                            toNodes = toNodesQueue.remove();
                            if (fromNode != null) {
                                for (Node toNode: toNodes) {
                                    try {
                                        circuit.connect(fromNode, toNode);
                                    } catch (InvalidConnectionException e) {
                                        LogUtils.logWarning(e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
