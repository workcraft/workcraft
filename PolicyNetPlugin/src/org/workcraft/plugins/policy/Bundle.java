package org.workcraft.plugins.policy;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

public class Bundle extends MathNode {
	private final Set<BundledTransition> transitions = new HashSet<BundledTransition>();
	private Color color = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());

	public Bundle() {
	}

	public void add(BundledTransition t) {
		transitions.add(t);
		sendNotification(new PropertyChangedEvent(this, "added"));
		t.sendNotification(new PropertyChangedEvent(this, "bundled"));
	}

	public void remove(BundledTransition t) {
		if (contains(t)) {
			transitions.remove(t);
			sendNotification(new PropertyChangedEvent(this, "removed"));
			t.sendNotification(new PropertyChangedEvent(this, "unbundled"));
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

	public void setColor(Color value) {
		this.color = value;
		sendNotification(new PropertyChangedEvent(this, "color"));
	}

	public Color getColor() {
		return color;
	}

}
