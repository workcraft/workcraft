package org.workcraft.dom.references;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.PageNode;

public class ReferenceHelper {

    private static final int MAX_STRING_LENGTH = 50;

    public static String getDefaultPrefix(Node node) {
        if (node instanceof Connection) return "con";
        if (node instanceof CommentNode) return "comment";
        if (node instanceof PageNode) return "pg";
        if (node instanceof Container) return "gr";
        return "node";
    }

    public static String getNodesAsString(final Model model, Collection<Node> nodes) {
        ArrayList<String> refs = getReferenceList(model, nodes);
        Collections.sort(refs);
        return getReferencesAsString(refs);
    }

    private static ArrayList<String> getReferenceList(final Model model, Collection<Node> nodes) {
        ArrayList<String> refs = new ArrayList<>();
        for (Node node: nodes) {
            String ref = model.getNodeReference(node);
            if (ref != null) {
                refs.add(ref);
            }
        }
        return refs;
    }

    public static String getReferencesAsString(Collection<String> refs) {
        String str = "";
        for (String ref: refs) {
            if (!str.isEmpty()) {
                str += ", ";
            }
            str += ref;
        }
        return wrapString(str, MAX_STRING_LENGTH);
    }

    private static String wrapString(String str, int len) {
        StringBuilder sb = new StringBuilder(str);
        int i = 0;
        while ((i + len < sb.length()) && ((i = sb.lastIndexOf(" ", i + len)) != -1)) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();
    }

}
