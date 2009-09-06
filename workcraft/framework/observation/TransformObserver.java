package org.workcraft.framework.observation;

import java.util.Collection;
import org.workcraft.dom.Node;

public interface TransformObserver {
	public Collection<Node> getObservedNodes();
	public void notify (TransformChangedEvent e);
}