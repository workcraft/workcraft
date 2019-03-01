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
import org.workcraft.utils.Hierarchy;
import org.workcraft.dom.references.Identifier;

public class NamespaceHelper {
    // Use negative lookahead (?![0-9]) to make sure that hierarchy separator is not followed by a number.
    // This is because "/[0-9]" is used in in STG for transition instances.
    private static final String LEGACY_HIERARCHY_SEPARATOR_REGEXP = "/(?![0-9])";
    private static final String LEGACY_FLATNAME_SEPARATOR_REGEXP = "__";

    private static final String HIERARCHY_SEPARATOR = ".";
    private static final String FLATNAME_SEPARATOR = "_";

    private static final Pattern HEAD_TAIL_PATTERN = Pattern.compile(
            "([^" + HIERARCHY_SEPARATOR + "]+)" + "(" + Pattern.quote(HIERARCHY_SEPARATOR) + "(.+))?");

    private static final int HEAD_GROUP = 1;
    private static final int TAIL_GROUP = 3;

    public static String convertLegacyHierarchySeparators(String ref) {
        return ref.replaceAll(LEGACY_HIERARCHY_SEPARATOR_REGEXP, HIERARCHY_SEPARATOR);
    }

    public static String convertLegacyFlatnameSeparators(String ref) {
        if (ref.contains(getHierarchySeparator())) {
            return ref;
        }
        return ref.replaceAll(LEGACY_FLATNAME_SEPARATOR_REGEXP, getHierarchySeparator());
    }

    public static String getHierarchySeparator() {
        return HIERARCHY_SEPARATOR;
    }

    public static String flattenReference(String reference) {
        return reference.replaceAll(Pattern.quote(HIERARCHY_SEPARATOR), FLATNAME_SEPARATOR);
    }

    public static LinkedList<String> splitReference(String reference) {
        LinkedList<String> result = new LinkedList<>();
        if ((reference != null) && !reference.isEmpty()) {
            Matcher matcher = HEAD_TAIL_PATTERN.matcher(reference);
            if (matcher.find()) {
                String head = matcher.group(HEAD_GROUP);
                result.add(head);
                String tail = matcher.group(TAIL_GROUP);
                if (tail != null) {
                    result.addAll(splitReference(tail));
                }
            }
        }
        return result;
    }

    public static String getParentReference(String reference) {
        String result = "";
        // legacy reference support
        if (!Identifier.isNumber(reference)) {
            LinkedList<String> path = splitReference(reference);
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
        String result = getParentReference(reference);
        if (result.length() > 0) {
            result += getHierarchySeparator();
        }
        return result;
    }

    public static String getReferenceHead(String reference) {
        // legacy reference support
        if (!Identifier.isNumber(reference)) {
            Matcher matcher = HEAD_TAIL_PATTERN.matcher(reference);
            if (matcher.find()) {
                return matcher.group(HEAD_GROUP);
            }
        }
        return reference;
    }

    public static String getReferenceTail(String reference) {
        // legacy reference support
        if (!Identifier.isNumber(reference)) {
            Matcher matcher = HEAD_TAIL_PATTERN.matcher(reference);
            if (matcher.find()) {
                String result = matcher.group(TAIL_GROUP);
                return (result == null) ? "" : result;
            }
        }
        return "";
    }

    public static String getReferenceName(String reference) {
        String head = getReferenceHead(reference);
        String tail = getReferenceTail(reference);
        if (tail.isEmpty()) {
            return head;
        }
        return getReferenceName(tail);
    }

    public static String getReference(String path, String name) {
        String result = "";
        boolean needsSeparator = false;
        if ((path != null) && !path.isEmpty()) {
            result += path;
            needsSeparator = true;
        }
        if ((name != null) && !name.isEmpty()) {
            if (needsSeparator) {
                result += getHierarchySeparator();
            }
            result += name;
        }
        return result;
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

    private static void copyPageStructure(VisualModel srcModel, Container srcContainer,
            VisualModel dstModel, Container dstContainer) {

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
            String ref = model.getMathReference(page);
            result.put(ref, page);
        }
        return result;
    }

    public static boolean isHierarchical(String ref) {
        return (ref != null) && !ref.isEmpty() && ref.substring(1).contains(HIERARCHY_SEPARATOR);
    }

}
