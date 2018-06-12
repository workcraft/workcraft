package org.workcraft.dom.visual;

import java.awt.geom.Rectangle2D;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.connections.VisualConnection;

class Tools {

    static VisualGroup createGroup(Container parent) {
        VisualGroup node = new VisualGroup();
        if (parent != null) {
            parent.add(node);
        }
        return node;
    }

    static VisualComponent createComponent(VisualGroup parent) {
        SquareNode node = new SquareNode(parent, new Rectangle2D.Double(0, 0, 1, 1));
        parent.add(node);
        return node;
    }

    static VisualConnection createConnection(VisualComponent c1, VisualComponent c2, VisualGroup parent) {
        VisualConnection connection = new VisualConnection(null, c1, c2);
        parent.add(connection);
        return connection;
    }

}
