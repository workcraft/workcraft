package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

import java.util.Collection;
import java.util.HashSet;

public abstract class NodeStg {
    public abstract Collection<VisualSignalTransition> getAllTransitions();

    public abstract Collection<VisualPlace> getAllPlaces();

    public Collection<VisualNode> getAllNodes() {
        HashSet<VisualNode> result = new HashSet<>();
        result.addAll(getAllPlaces());
        result.addAll(getAllTransitions());
        return result;
    }

    public boolean contains(Node n) {
        if (n != null) {
            for (VisualPlace p: getAllPlaces()) {
                if ((n == p) || ((p != null) && (n == p.getReferencedPlace()))) {
                    return true;
                }
            }
            for (VisualSignalTransition t: getAllTransitions()) {
                if ((n == t) || ((t != null) && (n == t.getReferencedTransition()))) {
                    return true;
                }
            }
        }
        return false;
    }

}
