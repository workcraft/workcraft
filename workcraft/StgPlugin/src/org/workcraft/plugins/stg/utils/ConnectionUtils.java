package org.workcraft.plugins.stg.utils;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.VisualTransition;

public class ConnectionUtils extends org.workcraft.plugins.petri.utils.ConnectionUtils {

    public static boolean hasImplicitPlaceArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualTransition predTransition = null;
        VisualTransition succTransition = null;
        if (first instanceof VisualTransition) {
            predTransition = (VisualTransition) first;
        }
        if (second instanceof VisualTransition) {
            succTransition = (VisualTransition) second;
        }
        if ((predTransition != null) && (succTransition != null)) {
            VisualConnection connection = visualModel.getConnection(predTransition, succTransition);
            found = connection != null;
        }
        return found;
    }

}
