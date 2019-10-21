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
        if (first instanceof VisualXbmState && second instanceof VisualXbmState) {
            VisualXbmState from = (VisualXbmState) first;
            VisualXbmState to = (VisualXbmState) second;
            if ((first != null) && (second != null)) {
                Collection<VisualBurstEvent> events = Hierarchy.getDescendantsOfType(model.getRoot(), VisualBurstEvent.class);
                for (VisualBurstEvent c: events) {
                    if (c.getReferencedConnection().getFirst() instanceof XbmState && c.getReferencedConnection().getSecond() instanceof XbmState) {
                        found = found || (c.getReferencedConnection().getFirst() == from.getReferencedComponent()) && (c.getReferencedConnection().getSecond() == to.getReferencedComponent());
                    }
                }
            }
        }
        return found;
    }
}
