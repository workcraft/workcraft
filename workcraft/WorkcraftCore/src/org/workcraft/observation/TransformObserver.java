package org.workcraft.observation;

import org.workcraft.dom.visual.TransformDispatcher;

public interface TransformObserver {
    void subscribe(TransformDispatcher dispatcher);
    void notify(TransformEvent e);
}
