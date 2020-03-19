package org.workcraft.observation;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;

import java.util.Collection;

public class SelectionChangedEvent implements StateEvent {

    private final VisualModel sender;
    private final Collection<? extends VisualNode> prevSelection;

    public SelectionChangedEvent(VisualModel sender, Collection<? extends VisualNode> prevSelection) {
        this.sender = sender;
        this.prevSelection = prevSelection;
    }

    @Override
    public VisualModel getSender() {
        return sender;
    }

    public Collection<? extends VisualNode> getSelection() {
        return sender.getSelection();
    }

    public Collection<? extends VisualNode> getPrevSelection() {
        return prevSelection;
    }

}
