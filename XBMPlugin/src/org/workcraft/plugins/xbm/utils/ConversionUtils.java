package org.workcraft.plugins.xbm.utils;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.xbm.VisualBurstEvent;
import org.workcraft.plugins.xbm.VisualXbmState;
import org.workcraft.plugins.xbm.XbmState;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;

public class ConversionUtils {

    public static boolean doesArcExist(VisualModel model, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualXbmState from = null;
        VisualXbmState to = null;
        if (first instanceof VisualXbmState) {
            from = (VisualXbmState) first;
        }
        if (second instanceof VisualXbmState) {
            to = (VisualXbmState) second;
        }
        if ((first != null) && (second != null)) {
            Collection<VisualBurstEvent> events = Hierarchy.getDescendantsOfType(model.getRoot(), VisualBurstEvent.class);
            for (VisualBurstEvent c: events) {
                if (c.getReferencedBurstEvent().getFirst() instanceof XbmState && c.getReferencedBurstEvent().getSecond() instanceof XbmState) {
                    found = found || (c.getReferencedBurstEvent().getFirst() == from.getReferencedState()) && (c.getReferencedBurstEvent().getSecond() == to.getReferencedState());
                }
            }
        }
        return found;
    }
}
