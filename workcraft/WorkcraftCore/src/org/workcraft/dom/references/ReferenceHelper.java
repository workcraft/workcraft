package org.workcraft.dom.references;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collection;

public class ReferenceHelper {

    public static String getNodesAsWrapString(final Model model, Collection<? extends Node> nodes) {
        return TextUtils.wrapLine(getNodesAsString(model, nodes));
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

}
