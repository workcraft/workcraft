package org.workcraft.plugins.policy;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

public class Bundle extends MathNode  {
	private final Set<BundledTransition> transitions = new HashSet<BundledTransition>();
	private Color color = CieColorUtils.getLabColor(0.7f, (float)Math.random(), (float)Math.random());
//	private Color color = Color.getHSBColor((float)Math.random(), 0.25f, 0.75f);

	public Bundle() {
	}

	public void add(BundledTransition transition) {
		if (transition != null) {
			transitions.add(transition);
			sendNotification(new PropertyChangedEvent(this, "added"));
			transition.sendNotification(new PropertyChangedEvent(this, "bundled"));
		}
	}

	public void remove(BundledTransition transition) {
		if (contains(transition)) {
			transitions.remove(transition);
			sendNotification(new PropertyChangedEvent(this, "removed"));
			transition.sendNotification(new PropertyChangedEvent(this, "unbundled"));
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

	public void setColor(Color value) {
		this.color = value;
		sendNotification(new PropertyChangedEvent(this, "color"));
	}

	public Color getColor() {
		return color;
	}

}
