package org.workcraft.plugins.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathNode;

public class Bundle extends MathNode {
	protected final Set<BundledTransition> transitions = new HashSet<BundledTransition>();

	public Bundle() {
	}

	public void add(BundledTransition t) {
		transitions.add(t);
	}

	public void remove(BundledTransition t) {
		transitions.remove(t);
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
