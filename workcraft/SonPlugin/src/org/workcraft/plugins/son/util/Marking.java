package org.workcraft.plugins.son.util;

import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.elements.PlaceNode;

public class Marking extends ArrayList<PlaceNode> {

    private static final long serialVersionUID = 3343743813175510454L;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Marking marking)) return false;

        if (marking.size() != size()) return false;

        for (Node node : marking) {
            if (!this.contains(node)) {
                return false;
            }
        }
        return true;
    }

}
