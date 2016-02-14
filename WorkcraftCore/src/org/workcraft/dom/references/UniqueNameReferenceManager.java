package org.workcraft.dom.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

public class UniqueNameReferenceManager extends HierarchySupervisor implements ReferenceManager {
    private References refs;
    final private UniqueNameManager mgr;

    public UniqueNameReferenceManager(References refs) {
        this.refs = refs;
        this.mgr = new UniqueNameManager() {
            @Override
            public String getPrefix(Node node) {
                return UniqueNameReferenceManager.this.getPrefix(node);
            }
        };
    }

    public NameManager getNameManager() {
        return mgr;
    }

    @Override
    public void attach(Node root) {
        if (refs != null) {
            setExistingReference(root);
            for (Node n : Hierarchy.getDescendantsOfType(root, Node.class)) {
                setExistingReference(n);
            }
            refs = null;
        }
        super.attach(root);
    }

    private void setExistingReference(Node n) {
        final String reference = refs.getReference(n);
        if (reference != null) {
            mgr.setName(n, reference);
        }
    }

    @Override
    public Node getNodeByReference(NamespaceProvider provider, String reference) {
        return mgr.getNode(reference);
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        return mgr.getName(node);
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesAddedEvent) {
            for (Node node : e.getAffectedNodes()) {
                mgr.setDefaultNameIfUnnamed(node);
                for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class)) {
                    mgr.setDefaultNameIfUnnamed(node2);
                }
            }
        }
        if (e instanceof NodesDeletedEvent) {
            for (Node node : e.getAffectedNodes()) {
                mgr.remove(node);
                for (Node node2 : Hierarchy.getDescendantsOfType(node, Node.class)) {
                    mgr.remove(node2);
                }
            }
        }
    }

    @Override
    public String getPrefix(Node node) {
        return ReferenceHelper.getDefaultPrefix(node);
    }

    public void setName(Node node, String name) {
        mgr.setName(node, name);
    }

    public String getName(Node node) {
        return mgr.getName(node);
    }

}
