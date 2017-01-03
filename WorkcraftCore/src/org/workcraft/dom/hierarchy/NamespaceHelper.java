package org.workcraft.dom.hierarchy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;

public class NamespaceHelper {
    // Use negative lookahead (?![0-9]) to make sure that hierarchy separator is not followed by a number.
    // "/[0-9]" is used in in STG for transition instances.
    private static final String LEGACY_HIERARCHY_SEPARATOR_PATTERN = "/(?![0-9])";
    private static final String LEGACY_FLATNAME_SEPARATOR_PATTERN = "__";
    private static final String HIERARCHY_SEPARATOR = ".";
    private static final Pattern HIERARCHY_PATTERN = Pattern.compile(
            "(" + Pattern.quote(HIERARCHY_SEPARATOR) + ")?" +
            "(([_A-Za-z][_A-Za-z0-9]*)([\\+\\-\\~])?(/[0-9]+)?)(.*)");

    public static String convertLegacyHierarchySeparators(String ref) {
        return ref.replaceAll(LEGACY_HIERARCHY_SEPARATOR_PATTERN, HIERARCHY_SEPARATOR);
    }

    public static String convertLegacyFlatnameSeparators(String ref) {
        return ref.replaceAll(LEGACY_FLATNAME_SEPARATOR_PATTERN, HIERARCHY_SEPARATOR);
    }

    public static String getHierarchySeparator() {
        return HIERARCHY_SEPARATOR;
    }

    public static String hierarchicalToFlatName(String reference) {
        return reference;
    }

    public static void splitReference(String reference, LinkedList<String> path) {
        if (!reference.isEmpty()) {
            Matcher matcher = HIERARCHY_PATTERN.matcher(reference);
            if (matcher.find()) {
                path.add(matcher.group(2));
                splitReference(matcher.group(6), path);
            }
        }
    }

    public static String getParentReference(String reference) {
        String result = "";
        // legacy reference support
        if (!Identifier.isNumber(reference)) {
            LinkedList<String> path = new LinkedList<>();
            splitReference(reference, path);

            for (int i = 0; i < path.size() - 1; i++) {
                result += path.get(i);
                if (i < path.size() - 2) {
                    result += getHierarchySeparator();
                }
            }
        }
        return result;
    }

    public static String getReferencePath(String reference) {
        String ret = getParentReference(reference);
        if (ret.length() > 0) {
            ret += getHierarchySeparator();
        }
        return ret;
    }

    public static String getReferenceHead(String reference) {
        // legacy reference support
        if (Identifier.isNumber(reference)) {
            return reference;
        }

        Matcher matcher = HIERARCHY_PATTERN.matcher(reference);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String getReferenceTail(String reference) {
        // legacy reference support
        if (Identifier.isNumber(reference)) {
            return "";
        }

        Matcher matcher = HIERARCHY_PATTERN.matcher(reference);
        if (matcher.find()) {
            return matcher.group(6);
        }
        return null;
    }

    public static String getReferenceName(String reference) {
        String head = getReferenceHead(reference);
        String tail = getReferenceTail(reference);
        if (tail.isEmpty()) {
            return head;
        }
        return getReferenceName(tail);
    }

    public static Container getMathContainer(VisualModel visualModel, Container visualContainer) {
        if (visualContainer == null) {
            visualContainer = visualModel.getRoot();
        }

        // Find the closest container that has a referenced math node.
        VisualComponent vis = null;
        if (visualContainer instanceof VisualComponent) {
            vis = (VisualComponent) visualContainer;
        } else {
            vis = Hierarchy.getNearestAncestor(visualContainer, VisualComponent.class);
        }

        // Get appropriate math container, it will be the target container for the math model.
        MathModel mmodel = visualModel.getMathModel();
        Container mathTargetContainer = mmodel.getRoot();
        if (vis != null) {
            mathTargetContainer = (Container) vis.getReferencedComponent();
        }
        return mathTargetContainer;
    }

    private static void copyPageStructure(VisualModel srcModel, Container srcContainer, VisualModel dstModel, Container dstContainer) {
        HashMap<Container, Container> toProcess = new HashMap<>();
        for (Node srcNode: srcContainer.getChildren()) {
            if (srcNode instanceof VisualPage) {
                VisualPage srcPage = (VisualPage) srcNode;
                String name = srcModel.getMathName(srcPage);

                VisualPage dstPage = new VisualPage(new PageNode());
                dstContainer.add(dstPage);
                dstPage.copyPosition(srcPage);
                dstPage.copyStyle(srcPage);

                Container dstMathContainer = NamespaceHelper.getMathContainer(dstModel, dstContainer);
                dstMathContainer.add(dstPage.getReferencedComponent());
                dstModel.setMathName(dstPage, name);

                toProcess.put(srcPage, dstPage);
            } else if (srcNode instanceof VisualGroup) {
                VisualGroup srcGroup = (VisualGroup) srcNode;
                toProcess.put(srcGroup, dstContainer);
            }
        }

        for (Entry<Container, Container> en: toProcess.entrySet()) {
            copyPageStructure(srcModel, en.getKey(), dstModel, en.getValue());
        }
    }

    public static void copyPageStructure(VisualModel srcModel, VisualModel dstModel) {
        Container dstContainer = dstModel.getRoot();
        Container srcContainer = srcModel.getRoot();
        copyPageStructure(srcModel, srcContainer, dstModel, dstContainer);
    }

    public  static HashMap<String, Container> getRefToPageMapping(VisualModel model) {
        HashMap<String, Container> result = new HashMap<>();
        Container root = model.getRoot();
        result.put("", root);
        for (VisualPage page: Hierarchy.getDescendantsOfType(root, VisualPage.class)) {
            String ref = model.getNodeMathReference(page);
            result.put(ref, page);
        }
        return result;
    }

}
