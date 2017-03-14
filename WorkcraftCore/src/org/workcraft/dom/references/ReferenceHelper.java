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
import org.workcraft.util.Identifier;

public class ReferenceHelper {

    public static String getDefaultPrefix(Node node) {
        if (node instanceof PageNode) return "pg";
        if (node instanceof Connection) return Identifier.createInternal("c");
        if (node instanceof CommentNode) return Identifier.createInternal("comment");
        if (node instanceof Container) return Identifier.createInternal("group");
        return "node";
    }

    public static String getNodesAsString(final Model model, Collection<Node> nodes, int len) {
        return wrapString(getNodesAsString(model, nodes), len);
    }

    public static String getNodesAsString(final Model model, Collection<Node> nodes) {
        ArrayList<String> refs = getReferenceList(model, nodes);
        Collections.sort(refs);
        return getReferencesAsString(refs);
    }

    public static ArrayList<String> getReferenceList(final Model model, Collection<Node> nodes) {
        ArrayList<String> refs = new ArrayList<>();
        for (Node node: nodes) {
            String ref = model.getNodeReference(node);
            if (ref != null) {
                refs.add(ref);
            }
        }
        return refs;
    }

    public static String getReferencesAsString(Collection<String> refs, int len) {
        return wrapString(getReferencesAsString(refs), len);
    }

    public static String getReferencesAsString(Collection<String> refs) {
        String str = "";
        for (String ref: refs) {
            if (!str.isEmpty()) {
                str += ", ";
            }
            str += ref;
        }
        return str;
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
