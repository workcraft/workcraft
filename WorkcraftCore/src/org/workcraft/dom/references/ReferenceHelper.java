package org.workcraft.dom.references;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.PageNode;

public class ReferenceHelper {

    static public String getDefaultPrefix(Node node) {
        if (node instanceof Connection) return "con";
        if (node instanceof CommentNode) return "comment";
        if (node instanceof PageNode) return "pg";
        if (node instanceof Container) return "gr";
        return "node";
    }

}
