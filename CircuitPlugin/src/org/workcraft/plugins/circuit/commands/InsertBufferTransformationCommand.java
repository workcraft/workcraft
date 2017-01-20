package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class InsertBufferTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Insert buffers into selected wires";
    }

    @Override
    public String getPopupName() {
        return "Insert buffer";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualCircuitConnection;
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
        Collection<Node> result = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            result.addAll(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualCircuitConnection.class));
            result.retainAll(visualModel.getSelection());
        }
        return result;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualCircuitConnection)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualCircuitConnection connection = (VisualCircuitConnection) node;
            Node fromNode = connection.getFirst();
            Node toNode = connection.getSecond();
            Container container = Hierarchy.getNearestContainer(fromNode, toNode);

            FunctionComponent mathComponent = new FunctionComponent();
            Container mathContainer = NamespaceHelper.getMathContainer(circuit, container);
            mathContainer.add(mathComponent);

            VisualFunctionComponent component = circuit.createVisualComponent(mathComponent, VisualFunctionComponent.class, container);
            Point2D pos = connection.getMiddleSegmentCenterPoint();
            component.setPosition(pos);

            VisualFunctionContact inputContact = circuit.getOrCreateContact(component, null, IOType.INPUT);
            inputContact.setPosition(new Point2D.Double(-1.5, 0.0));
            VisualFunctionContact outputContact = circuit.getOrCreateContact(component, null, IOType.OUTPUT);
            outputContact.setPosition(new Point2D.Double(1.5, 0.0));
            outputContact.setSetFunction(inputContact.getReferencedContact());

            LinkedList<Point2D> prefixControlPoints = ConnectionHelper.getPrefixControlPoints(connection, pos);
            LinkedList<Point2D> suffixControlPoints = ConnectionHelper.getSuffixControlPoints(connection, pos);
            circuit.remove(connection);
            try {
                VisualConnection inputConnection = (VisualCircuitConnection) circuit.connect(fromNode, inputContact);
                ConnectionHelper.addControlPoints(inputConnection, prefixControlPoints);
                VisualConnection outputConnection = (VisualCircuitConnection) circuit.connect(outputContact, toNode);
                ConnectionHelper.addControlPoints(outputConnection, suffixControlPoints);
            } catch (InvalidConnectionException e) {
                LogUtils.logWarningLine(e.getMessage());
            }
            boolean initToOne = inputContact.getReferencedFunctionContact().getInitToOne();
            outputContact.getReferencedFunctionContact().setInitToOne(initToOne);
        }
    }

}
