package org.workcraft.dom.references;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ReferenceHelper {

    public static String getNodesAsWrapString(final Model model, Collection<? extends Node> nodes) {
        return TextUtils.wrapLine(getNodesAsString(model, nodes));
    }

    public static String getNodesAsString(final Model model, Collection<? extends Node> nodes) {
        List<String> refs = getReferenceList(model, nodes);
        SortUtils.sortNatural(refs);
        return String.join(", ", refs);
    }

    public static List<String> getReferenceList(final Model model, Collection<? extends Node> nodes) {
        return nodes.stream().map(model::getNodeReference).collect(Collectors.toList());
    }

}
