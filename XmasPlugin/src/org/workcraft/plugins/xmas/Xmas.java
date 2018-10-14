package org.workcraft.plugins.xmas;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.xmas.components.*;
import org.workcraft.util.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;

public class Xmas extends AbstractMathModel {

    public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
        MathConnection con = new MathConnection((MathNode) first, (MathNode) second);
        Hierarchy.getNearestContainer(first, second).add(con);
        return con;
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
