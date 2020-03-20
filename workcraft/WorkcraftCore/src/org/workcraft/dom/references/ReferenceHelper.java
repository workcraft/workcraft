package org.workcraft.dom.references;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.utils.SortUtils;

import java.util.ArrayList;
import java.util.Collection;

public class ReferenceHelper {

    public static String getNodesAsString(final Model model, Collection<? extends Node> nodes, int len) {
        return wrapString(getNodesAsString(model, nodes), len);
    }

    public static String getNodesAsString(final Model model, Collection<? extends Node> nodes) {
        ArrayList<String> refs = getReferenceList(model, nodes);
        SortUtils.sortNatural(refs);
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

    public static String getTextWithReferences(String msg, Collection<String> refs) {
        return getTextWithReferences(msg, refs, SizeHelper.getWrapLength());
    }

    public static String getTextWithReferences(String msg, Collection<String> refs, int len) {
        if (refs.size() == 1) {
            msg += " '" + refs.iterator().next() + "'.";
        } else {
            msg = makePlural(msg) + ":";
            String str = String.join(", ", refs);
            if (msg.length() + str.length() > len) {
                msg += "\n";
            } else {
                msg += " ";
            }
            msg += ReferenceHelper.getReferencesAsString(refs, len);
        }
        return msg;
    }

    private static String makePlural(String s) {
        if (s.endsWith("y")) {
            s = s.substring(0, s.length() - 1) + "ie";
        }
        if (s.endsWith("s") || s.endsWith("x") || s.endsWith("z") || s.endsWith("ch") || s.endsWith("sh")) {
            s += "es";
        } else {
            s += "s";
        }
        return s;
    }

}
