package org.workcraft.observation;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;

public class SelectionChangedEvent implements StateEvent {
    private final VisualModel sender;
    private final Collection<Node> prevSelection;

    public SelectionChangedEvent(VisualModel sender, Collection<Node> prevSelection) {
        this.sender = sender;
        this.prevSelection = prevSelection;
    }

    public VisualModel getSender() {
        return sender;
    }

    public Collection<Node> getSelection() {
        return sender.getSelection();
    }

    public Collection<Node> getPrevSelection() {
        return prevSelection;
    }

}
