package org.workcraft.dom.visual.connections;

import org.workcraft.observation.SelectionChangedEvent;

public interface SelectionObserver {
	public void notify (SelectionChangedEvent event);
}