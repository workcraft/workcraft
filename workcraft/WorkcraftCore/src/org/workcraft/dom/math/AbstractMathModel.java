package org.workcraft.dom.math;

import org.workcraft.dom.*;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.serialisation.References;
import org.workcraft.types.MultiSet;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractMathModel extends AbstractModel<MathNode, MathConnection> implements MathModel {

    public AbstractMathModel() {
        this(null);
    }

    public AbstractMathModel(Container root) {
        this(root, (References) null);
    }

    public AbstractMathModel(Container root, References refs) {
        this(root, new HierarchyReferenceManager(refs));
    }

    public AbstractMathModel(Container root, ReferenceManager man) {
        super(root, man);
        new DefaultHangingConnectionRemover(this).attach(getRoot());
    }

    @Override
    public MathGroup createDefaultRoot() {
        return new MathGroup();
    }

    @Override
    public HierarchyReferenceManager createDefaultReferenceManager() {
        return new HierarchyReferenceManager();
    }

    @Override
    public HierarchyReferenceManager getReferenceManager() {
        return (HierarchyReferenceManager) super.getReferenceManager();
    }

    @Override
    public <T extends MathNode> T createNode(String name, Container container, Class<T> type) {
        if (container == null) {
            container = getRoot();
        }
        T node = null;
        try {
            node = NodeFactory.createNode(type);
            container.add(node);
            if (name != null) {
                setName(node, name);
            }
        } catch (NodeCreationException e) {
            String containerRef = getNodeReference(container);
            throw new RuntimeException("Cannot create math node '" + name + "'"
                    + " of type " + type.getSimpleName() + " in container '" + containerRef + "'.");
        }
        return node;
    }

    @Override
    public <T extends MathNode> T getOrCreateNode(String name, Container container, Class<T> type) {
        if (container == null) {
            container = getRoot();
        }
        String ref = (container == getRoot()) ? name : (getNodeReference(container) + name);
        MathNode node = getNodeByReference(ref);
        if (node == null) {
            return createNode(name, container, type);
        }
        if (type.isInstance(node)) {
            return type.cast(node);
        }
        throw new ArgumentException("Node '" + name + "' already exists and is not of type " + type.getSimpleName());
    }

    @Override
    public <T extends MathNode> T createNodeWithHierarchy(String ref, Container container, Class<T> type) {
        if (container == null) {
            container = getRoot();
        }
        if (!NamespaceHelper.isHierarchical(ref)) {
            return createNode(ref, container, type);
        } else {
            if (container instanceof NamespaceProvider) {
                String pageName = NamespaceHelper.getReferenceHead(ref);
                Node pageNode = getNodeByReference((NamespaceProvider) container, pageName);
                if (pageNode == null) {
                    pageNode = createNode(pageName, container, PageNode.class);
                }
                if (pageNode instanceof Container parentContainer) {
                    String tailRef = NamespaceHelper.getReferenceTail(ref);
                    return createNodeWithHierarchy(tailRef, parentContainer, type);
                } else {
                    throw new RuntimeException("Node '" + pageName + "' is not a valid container.");
                }
            } else {
                String containerRef = getNodeReference(container);
                throw new RuntimeException("Container '" + containerRef + "' is not a valid namespace provider.");
            }
        }
    }

    @Override
    public <T extends MathNode> T createMergedNode(Collection<MathNode> srcNodes, Container container, Class<T> type) {
        return createNode(null, container, type);
    }

    private void setNamespaceRecursively(HierarchyReferenceManager dstRefManager, Container dstContainer,
            Model<?, ?> srcModel, Container srcRoot, Collection<? extends MathNode> srcChildren) {

        // Collect the nodes to reparent - need to assign the whole tree to new providers
        Collection<Node> nodes = null;
        if (srcChildren != null) {
            nodes = new HashSet<>(srcChildren);
        } else {
            nodes = Hierarchy.getChildrenOfType(srcRoot, Node.class);
        }

        NamespaceProvider dstProvider = dstRefManager.getNamespaceProvider(dstContainer);
        if (dstContainer instanceof NamespaceProvider) {
            dstProvider = (NamespaceProvider) dstContainer;
        }

        HierarchyReferenceManager srcRefManager = (HierarchyReferenceManager) srcModel.getReferenceManager();
        dstRefManager.setNamespaceProvider(nodes, srcRefManager, dstProvider);

        srcRoot.reparent(nodes, dstContainer);

        // Propagate the name data into the nodes. This may be necessary after setDefaultNameIfUnnamed was called.
        for (Node node : nodes) {
            dstRefManager.setName(node, dstRefManager.getName(node));
        }

        for (Node node: nodes) {
            if (node instanceof Container container) {
                setNamespaceRecursively(dstRefManager, container, srcModel, container, null);
            }
        }
    }

    @Override
    public boolean reparent(Container dstContainer, Model<?, ?> srcModel, Container srcRoot, Collection<? extends MathNode> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        HierarchyReferenceManager manager = getReferenceManager();
        if (manager == null) {
            return false;
        }
        NamespaceProvider provider = null;
        if (dstContainer instanceof NamespaceProvider) {
            provider = (NamespaceProvider) dstContainer;
        } else {
            provider = manager.getNamespaceProvider(dstContainer);
        }
        setNamespaceRecursively(manager, provider, srcModel, srcRoot, srcChildren);
        return true;
    }

    public MultiSet<String> getStatistics() {
        MultiSet<String> result = new MultiSet<>();
        for (MathNode node: Hierarchy.getDescendantsOfType(getRoot(), MathNode.class)) {
            String categoryName = node.getClass().getSimpleName();
            result.add(categoryName);
        }
        return result;
    }

    @Override
    public void validateConnection(MathNode first, MathNode second) throws InvalidConnectionException {
        if ((first == null) || (first instanceof MathConnection) || (second == null) || (second instanceof MathConnection)) {
            throw new InvalidConnectionException("Invalid connection");
        }
    }

    @Override
    public MathConnection connect(MathNode first, MathNode second) throws InvalidConnectionException {
        validateConnection(first, second);
        MathConnection connection = new MathConnection(first, second);
        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(connection);
        return connection;
    }

    @Override
    public void anonymise() {
        setTitle("");
        for (MathNode node : Hierarchy.getDescendantsOfType(getRoot(), MathNode.class)) {
            String name = getName(node);
            if (!Identifier.hasInternalPrefix(name)) {
                getReferenceManager().setDefaultName(node);
            }
        }
    }

}
