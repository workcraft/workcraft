package org.workcraft.dom.visual.connections;

import org.workcraft.observation.SelectionChangedEvent;

public interface SelectionObserver {
    void notify(SelectionChangedEvent event);
}
