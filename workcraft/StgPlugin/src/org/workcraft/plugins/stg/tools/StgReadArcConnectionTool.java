package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriReadArcConnectionTool;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;

public class StgReadArcConnectionTool extends PetriReadArcConnectionTool {

    @Override
    public boolean isConnectable(Node node) {
        return (node instanceof VisualPlace)
              || (node instanceof VisualReplicaPlace)
              || (node instanceof VisualTransition)
              || (node instanceof VisualImplicitPlaceArc);
    }

}
