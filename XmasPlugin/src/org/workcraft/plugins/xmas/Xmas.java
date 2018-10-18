package org.workcraft.plugins.xmas;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.xmas.components.*;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;

public class Xmas extends AbstractMathModel {

    public Xmas() {
        this(null, null);
    }

    public Xmas(Container root, References refs) {
        super(root, refs);
    }

    @Override
    public void validateConnection(MathNode first, MathNode second) throws InvalidConnectionException {
        super.validateConnection(first, second);

        if (!(first instanceof XmasContact) || !(second instanceof XmasContact)) {
            throw new InvalidConnectionException("Connection is only allowed between ports");
        }

        if (((XmasContact) first).getIOType() != XmasContact.IOType.OUTPUT) {
            throw new InvalidConnectionException("Connection is only allowed from output port");
        }

        if (((XmasContact) second).getIOType() != XmasContact.IOType.INPUT) {
            throw new InvalidConnectionException("Connection is only allowed to input port");
        }

        for (Connection c: this.getConnections(first)) {
            if (c.getFirst() == first) {
                throw new InvalidConnectionException("Only one connection is allowed from port");
            }
        }

        for (Connection c: this.getConnections(second)) {
            if (c.getSecond() == second) {
                throw new InvalidConnectionException("Only one connection is allowed to port");
            }
        }
    }

    public Collection<Node> getNodes() {
        ArrayList<Node> result = new ArrayList<>();
        for (Node node : Hierarchy.getDescendantsOfType(getRoot(), Node.class)) {
            if (node instanceof SourceComponent) {
                result.add(node);
            }
            if (node instanceof FunctionComponent) {
                result.add(node);
            }
            if (node instanceof QueueComponent) {
                result.add(node);
            }
            if (node instanceof ForkComponent) {
                result.add(node);
            }
            if (node instanceof JoinComponent) {
                result.add(node);
            }
            if (node instanceof SwitchComponent) {
                result.add(node);
            }
            if (node instanceof MergeComponent) {
                result.add(node);
            }
            if (node instanceof SinkComponent) {
                result.add(node);
            }
        }
        return result;
    }

    public String getType(Node node) {
        String result = null;
        if (node instanceof SourceComponent) result = "source";
        if (node instanceof FunctionComponent) result = "function";
        if (node instanceof QueueComponent) result = "queue";
        if (node instanceof ForkComponent) result = "fork";
        if (node instanceof JoinComponent) result = "join";
        if (node instanceof SwitchComponent) result = "switch";
        if (node instanceof MergeComponent) result = "merge";
        if (node instanceof SwitchComponent) result = "switch";
        if (node instanceof SinkComponent) result = "sink";
        if (node instanceof CreditComponent) result = "credit";
        if (node instanceof SyncComponent) result = "sync";
        return result;
    }

    public Collection<SourceComponent> getSourceComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SourceComponent.class);
    }

    public Collection<FunctionComponent> getFunctionComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), FunctionComponent.class);
    }

    public Collection<SwitchComponent> getSwitchComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SwitchComponent.class);
    }

    public Collection<SinkComponent> getSinkComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SinkComponent.class);
    }

}
