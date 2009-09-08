package org.workcraft.dom.visual;

import org.workcraft.dom.Node;
import org.workcraft.framework.observation.TransformObserver;

public interface TransformDispatcher {
	public void subscribe (TransformObserver observer, Node observed);
	public void unsubscribe (TransformObserver observer, Node observed);
}