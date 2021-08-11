package org.workcraft.dom.references;

import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ReferenceHelper {

    public static String getNodesAsWrapString(MathModel model, Collection<? extends MathNode> nodes) {
        return TextUtils.wrapLine(getNodesAsString(model, nodes));
    }

    public static String getNodesAsString(MathModel model, Collection<? extends MathNode> nodes) {
        List<String> refs = getReferenceList(model, nodes);
        SortUtils.sortNatural(refs);
        return String.join(", ", refs);
    }

    public static List<String> getReferenceList(MathModel model, Collection<? extends MathNode> nodes) {
        return nodes.stream()
                .map(model::getNodeReference)
                .filter(Objects::nonNull)
                .map(Identifier::truncateNamespaceSeparator)
                .collect(Collectors.toList());
    }

    public static Set<String> getReferenceSet(MathModel model, Collection<? extends MathNode> nodes) {
        return nodes.stream()
                .map(model::getNodeReference)
                .filter(Objects::nonNull)
                .map(Identifier::truncateNamespaceSeparator)
                .collect(Collectors.toSet());
    }

}
