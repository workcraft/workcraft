package org.workcraft.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.workcraft.observation.ObservableHierarchy;

public class DefaultGroupImpl extends AbstractGroup implements ObservableHierarchy, Container {

    private final Collection<Node> children = new LinkedHashSet<>();

    public DefaultGroupImpl(Container groupRef) {
        super(groupRef);
    }

    @Override
    public Collection<Node> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    @Override
    protected void addInternal(Node node) {
        children.add(node);
    }

    @Override
    protected void removeInternal(Node node) {
        children.remove(node);
    }

}
