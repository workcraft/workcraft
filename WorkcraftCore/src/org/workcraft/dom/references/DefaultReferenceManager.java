package org.workcraft.dom.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.util.TwoWayMap;

public class DefaultReferenceManager extends HierarchySupervisor implements ReferenceManager {
    private final IDGenerator idGenerator = new IDGenerator();
    private final TwoWayMap<String, Node> nodes = new TwoWayMap<>();

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesAddedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesDeletedEvent) {
            for (Node n: e.getAffectedNodes()) {
                nodeRemoved(n);
            }
        }
    }

    private void nodeRemoved(Node node) {
        nodes.removeValue(node);
        for (Node n: node.getChildren()) {
            nodeRemoved(n);
        }
    }

    private void nodeAdded(Node node) {
        String id = Integer.toString(idGenerator.getNextID());
        nodes.put(id, node);
        for (Node n : node.getChildren()) {
            nodeAdded(n);
        }
    }

    @Override
    public Node getNodeByReference(NamespaceProvider provider, String reference) {
        return nodes.getValue(reference);
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        return nodes.getKey(node);
    }

    @Override
    public String getPrefix(Node node) {
        return ReferenceHelper.getDefaultPrefix(node);
    }

}
