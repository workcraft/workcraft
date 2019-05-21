package org.workcraft.dom.references;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReferenceHelper {

    public static String getNodesAsString(final Model model, Collection<? extends Node> nodes, int len) {
        return wrapString(getNodesAsString(model, nodes), len);
    }

    public static String getNodesAsString(final Model model, Collection<? extends Node> nodes) {
        ArrayList<String> refs = getReferenceList(model, nodes);
        Collections.sort(refs);
        return String.join(", ", refs);
    }

    public static ArrayList<String> getReferenceList(final Model model, Collection<? extends Node> nodes) {
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
        return wrapString(String.join(", ", refs), len);
    }

    private static String wrapString(String str, int len) {
        StringBuilder sb = new StringBuilder(str);
        int i = 0;
        while ((i + len < sb.length()) && ((i = sb.lastIndexOf(" ", i + len)) != -1)) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();
    }

    public static List<String> parseReferenceList(String str) {
        List<String> result = new ArrayList<>();
        if ((str != null) && !str.isEmpty()) {
            for (String name : str.replaceAll("\\s", "").split(",")) {
                if (Identifier.isName(name)) {
                    result.add(name);
                } else {
                    return null;
                }
            }
        }
        return result;
    }

}
