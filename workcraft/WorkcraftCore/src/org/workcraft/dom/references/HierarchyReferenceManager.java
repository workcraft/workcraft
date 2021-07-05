package org.workcraft.dom.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.serialisation.References;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashMap;

public class HierarchyReferenceManager extends HierarchySupervisor implements ReferenceManager {

    private final HashMap<NamespaceProvider, NameManager> managers = new HashMap<>();

    // every node belongs to some name space provider (except the main root node of the model)
    private final HashMap<Node, NamespaceProvider> node2namespace = new HashMap<>();
    private NamespaceProvider topProvider; // namespace provided by root
    private References refs;

    public HierarchyReferenceManager() {
        this(null);
    }

    public HierarchyReferenceManager(References refs) {
        this.refs = refs;
    }

    public NamespaceProvider getNamespaceProvider(Node node) {
        NamespaceProvider provider = node2namespace.get(node);
        if (provider == null) {
            Node container = node.getParent();
            if (container != null) {
                provider = Hierarchy.getNearestAncestor(container, NamespaceProvider.class);
            }
        }
        node2namespace.put(node, provider);
        return provider;
    }

    public void setNamespaceProvider(Collection<Node> nodes, NamespaceProvider provider) {
        setNamespaceProvider(nodes, this, provider);
    }

    public void setNamespaceProvider(Collection<Node> nodes,
            HierarchyReferenceManager srcRefManager, NamespaceProvider dstProvider) {

        if (dstProvider == null) {
            dstProvider = topProvider;
        }
        for (Node node : nodes) {
            NamespaceProvider srcProvider = srcRefManager.getNamespaceProvider(node);
            if (srcProvider != null) {
                String name = srcRefManager.getName(node);

                // Clear cached data in the local and the source reference manager.
                node2namespace.remove(node);
                srcRefManager.node2namespace.remove(node);

                // Do not assign name if it was not assigned in the first place (e.g. for an implicit place).
                if ((name != null) && ((dstProvider != srcProvider) || (node2namespace != srcRefManager.node2namespace))) {
                    NameManager srcNameManager = srcRefManager.getNameManager(srcProvider);
                    NameManager dstNameManager = this.getNameManager(dstProvider);
                    srcNameManager.remove(node);
                    Node clashingNode = dstNameManager.getNode(name);
                    if (nodes.contains(clashingNode)) {
                        String newName = dstNameManager.getDerivedName(clashingNode, name);
                        dstNameManager.setName(clashingNode, newName, true);
                    }
                    String newName = dstNameManager.getDerivedName(node, name);
                    // Allow flexibility on naming nodes in case there is name clash in dstNameManager
                    dstNameManager.setName(node, newName, false);
                }
            }
        }
    }

    @Override
    public void attach(Node root) {
        // Root must be a namespace provider.
        topProvider = (NamespaceProvider) root;
        if (refs != null) {
            for (Node n : Hierarchy.getDescendantsOfType(root, Node.class)) {
                setExistingReference(n);
            }
            refs = null;
        }
        super.attach(root);
    }

    public NameManager getNameManager(NamespaceProvider provider) {
        if (provider == null) {
            provider = topProvider;
        }
        NameManager man = managers.get(provider);
        if (man == null) {
            man = createNameManager();
            managers.put(provider, man);
        }
        return man;
    }

    public NameManager getNameManager(Node node) {
        return getNameManager(getNamespaceProvider(node));
    }

    protected NameManager createNameManager() {
        return new DefaultNameManager();
    }

    protected void setExistingReference(Node node) {
        String reference = refs.getReference(node);
        if (reference != null) {
            String name = NamespaceHelper.getReferenceName(reference);
            setName(node, name);
        }
    }

    @Override
    public Node getNodeByReference(NamespaceProvider provider, String reference) {
        if (provider == null) {
            provider = topProvider;
        }
        if (reference.isEmpty() || reference.equals(NamespaceHelper.getHierarchySeparator())) {
            return provider;
        }
        String head = NamespaceHelper.getReferenceHead(reference);
        String tail = NamespaceHelper.getReferenceTail(reference);
        NameManager man = getNameManager(provider);
        Node node = man.getNode(head);
        if (node instanceof NamespaceProvider) {
            return getNodeByReference((NamespaceProvider) node, tail);
        }
        return node;
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        if (node == topProvider) {
            return NamespaceHelper.getHierarchySeparator();
        }
        if (provider == null) {
            provider = topProvider;
        }
        NamespaceProvider component = null;
        String result = "";
        do {
            component = getNamespaceProvider(node);
            if (component != null) {
                String name = getNameManager(component).getName(node);
                // The unnamed component just returns null.
                if (name == null) return null;
                result = name + result;
                node = node.getParent();
            }
        } while ((node != null) && (component != null) && (component != provider));
        return result;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesAddedEvent) {
            for (Node node : e.getAffectedNodes()) {
                if (node.getParent() != null) {
                    // if it is not a root node
                    NameManager man = getNameManager(node);
                    man.setDefaultNameIfUnnamed(node);
                    // additional call to propagate the name data after calling setDefaultNameIfUnnamed
                    setName(node, man.getName(node));
                }
                for (Node childNode : Hierarchy.getDescendantsOfType(node, Node.class)) {
                    NameManager mgr = getNameManager(childNode);
                    mgr.setDefaultNameIfUnnamed(childNode);
                    // additional call to propagate the name data after calling setDefaultNameIfUnnamed
                    setName(childNode, mgr.getName(childNode));
                }
            }
        }

        if (e instanceof NodesDeletedEvent) {
            for (Node node : e.getAffectedNodes()) {
                getNameManager(node).remove(node);
                node2namespace.remove(node);
                for (Node childNode : Hierarchy.getDescendantsOfType(node, Node.class)) {
                    getNameManager(childNode).remove(childNode);
                    node2namespace.remove(childNode);
                }
            }
        }
    }

    public void setName(Node node, String name) {
        setName(node, name, true);
    }

    public void setName(Node node, String name, boolean force) {
        NameManager mgr = getNameManager(node);
        mgr.setName(node, name, force);
    }

    public String getName(Node node) {
        NameManager mgr = getNameManager(node);
        return mgr.getName(node);
    }

    public void setDefaultName(MathNode node) {
        NameManager nameManager = getNameManager(node);
        nameManager.setDefaultName(node);
    }

}
