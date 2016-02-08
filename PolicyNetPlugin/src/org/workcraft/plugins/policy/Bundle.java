package org.workcraft.plugins.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

public class Bundle extends MathNode  {
    public static final String PROPERTY_ADDED = "added";
    public static final String PROPERTY_REMOVED = "removed";
    public static final String PROPERTY_BUNDLED = "bundled";
    public static final String PROPERTY_UNBUNDLED = "unbundled";

    private final Set<BundledTransition> transitions = new HashSet<BundledTransition>();

    public void add(BundledTransition transition) {
        if (transition != null) {
            transitions.add(transition);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ADDED));
            transition.sendNotification(new PropertyChangedEvent(this, PROPERTY_BUNDLED));
        }
    }

    public void remove(BundledTransition transition) {
        if (contains(transition)) {
            transitions.remove(transition);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_REMOVED));
            transition.sendNotification(new PropertyChangedEvent(this, PROPERTY_UNBUNDLED));
        }
    }

    public void removeAll(Collection<BundledTransition> transitions) {
        for (BundledTransition transition: transitions) {
            remove(transition);
        }
    }

    public boolean contains(BundledTransition t) {
        return transitions.contains(t);
    }

    public boolean isEmpty() {
        return transitions.isEmpty();
    }

    public Collection<BundledTransition> getTransitions() {
        return Collections.unmodifiableCollection(transitions);
    }

}
