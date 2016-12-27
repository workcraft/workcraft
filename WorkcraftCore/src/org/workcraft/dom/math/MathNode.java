package org.workcraft.dom.math;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;

/**
 * Base type for mathematical objects -- components (graph nodes)
 * and connections (graph arcs).
 * @author Ivan Poliakov
 *
 */
public abstract class MathNode implements Node, ObservableState {
    private final ObservableStateImpl observableStateImpl = new ObservableStateImpl();

    private Node parent = null;

    public Collection<Node> getChildren() {
        return new HashSet<Node>();
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public void addObserver(StateObserver obs) {
        observableStateImpl.addObserver(obs);
    }

    @Override
    public void removeObserver(StateObserver obs) {
        observableStateImpl.removeObserver(obs);
    }

    @Override
    public final void sendNotification(StateEvent e) {
        observableStateImpl.sendNotification(e);
    }

}
