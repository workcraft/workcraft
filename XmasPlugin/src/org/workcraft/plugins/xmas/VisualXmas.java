package org.workcraft.plugins.xmas;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.xmas.components.*;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;
import org.workcraft.plugins.xmas.tools.SyncSelectionTool;
import org.workcraft.plugins.xmas.tools.XmasConnectionTool;
import org.workcraft.plugins.xmas.tools.XmasSimulationTool;
import org.workcraft.util.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("xMAS Circuit")
@ShortName("xMAS")
public class VisualXmas extends AbstractVisualModel {

    public VisualXmas(Xmas model) {
        this(model, null);
    }

    public VisualXmas(Xmas model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SyncSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new XmasConnectionTool());

        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SourceComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SinkComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(FunctionComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(QueueComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(ForkComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(JoinComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SwitchComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(MergeComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(CreditComponent.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(SyncComponent.class)));

        tools.add(new XmasSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (!(first instanceof VisualXmasContact) || !(second instanceof VisualXmasContact)) {
            throw new InvalidConnectionException("Connection is only allowed between ports");
        } else {
            if (((VisualXmasContact) first).getIOType() != IOType.OUTPUT) {
                throw new InvalidConnectionException("Connection is only allowed from output port.");
            }
            if (((VisualXmasContact) second).getIOType() != IOType.INPUT) {
                throw new InvalidConnectionException("Connection is only allowed to input port.");
            }
            for (Connection c: this.getConnections(first)) {
                if (c.getFirst() == first) {
                    throw new InvalidConnectionException("Only one connection is allowed from port.");
                }
            }
            for (Connection c: this.getConnections(second)) {
                if (c.getSecond() == second) {
                    throw new InvalidConnectionException("Only one connection is allowed to port.");
                }
            }
        }
    }

    @Override
    public Xmas getMathModel() {
        return (Xmas) super.getMathModel();
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);
        VisualXmasConnection connection = null;
        if (first instanceof VisualComponent && second instanceof VisualComponent) {
            VisualComponent c1 = (VisualComponent) first;
            VisualComponent c2 = (VisualComponent) second;
            if (mConnection == null) {
                mConnection = getMathModel().connect(c1.getReferencedComponent(), c2.getReferencedComponent());
            }
            connection = new VisualXmasConnection(mConnection, c1, c2);
            Node parent = Hierarchy.getCommonParent(c1, c2);
            VisualGroup nearestAncestor = Hierarchy.getNearestAncestor(parent, VisualGroup.class);
            nearestAncestor.add(connection);
        }
        return connection;
    }

    public VisualGroup getGroup(VisualComponent vsc) {
        return Hierarchy.getNearestAncestor(vsc, VisualGroup.class);
    }

    public Collection<VisualXmasComponent> getNodes() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualXmasComponent.class);
    }

}
