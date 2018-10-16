package org.workcraft.dom.math;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.util.Hierarchy;

public class MathGroup extends MathNode implements NamespaceProvider, ObservableHierarchy {
    private final DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    @Override
    public void add(Node node) {
        groupImpl.add(node);
    }

    @Override
    public void addObserver(HierarchyObserver obs) {
        groupImpl.addObserver(obs);
    }

    @Override
    public Collection<Node> getChildren() {
        return groupImpl.getChildren();
    }

    @Override
    public Node getParent() {
        return groupImpl.getParent();
    }

    @Override
    public void remove(Node node) {
        groupImpl.remove(node);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        groupImpl.removeObserver(obs);
    }

    @Override
    public void removeAllObservers() {
        groupImpl.removeAllObservers();
    }

    @Override
    public void setParent(Node parent) {
        groupImpl.setParent(parent);
    }

    @Override
    public void add(Collection<Node> nodes) {
        groupImpl.add(nodes);
    }

    @Override
    public void remove(Collection<Node> nodes) {
        groupImpl.remove(nodes);
    }

    @Override
    public void reparent(Collection<Node> nodes, Container newParent) {
        groupImpl.reparent(nodes, newParent);
    }

    @Override
    public void reparent(Collection<Node> nodes) {
        groupImpl.reparent(nodes);
    }

    public ArrayList<Node> unGroup(ReferenceManager manager) {
        ArrayList<Node> nodesToReparent = new ArrayList<>(groupImpl.getChildren());
        Container newParent = Hierarchy.getNearestAncestor(getParent(), Container.class);
        if (manager != null) {
            if (manager instanceof HierarchyReferenceManager) {
                HierarchyReferenceManager hierManager = (HierarchyReferenceManager) manager;
                NamespaceProvider provider = hierManager.getNamespaceProvider(newParent);
                hierManager.setNamespaceProvider(nodesToReparent, provider);
            }
        }
        groupImpl.reparent(nodesToReparent, newParent);
        return nodesToReparent;
    }

}
