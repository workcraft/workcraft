package org.workcraft.plugins.xbm;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class VisualBurstEvent extends VisualEvent {

    public VisualBurstEvent() {
        this(null, null, null);
    }

    public VisualBurstEvent(BurstEvent mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualBurstEvent(BurstEvent mathConnection, VisualState first, VisualState second) {
        super(mathConnection, first, second);
    }

    public BurstEvent getReferencedBurstEvent() {
        return (BurstEvent) getReferencedEvent();
    }

    private Burst getReferencedBurst() {
        return getReferencedBurstEvent().getBurst();
    }

    private Set<Signal> getReferencedSignals() {
        return getReferencedBurst().getSignals();
    }

    @Override
    public String getLabel(DrawRequest r) {
        return getReferencedBurstEvent().getAsString();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualBurstEvent) {
             VisualBurstEvent srcBurstEvent = (VisualBurstEvent) src;
             getReferencedBurstEvent();
        }
    }
}
