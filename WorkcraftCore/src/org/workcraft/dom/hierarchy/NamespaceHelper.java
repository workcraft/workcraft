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
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;

public class NamespaceHelper {
    // TODO: make it work with the embedded ' characters
    private static Pattern hPattern = Pattern.compile("(/)?(((\\'([^\\']+)\\')|([_A-Za-z][_A-Za-z0-9]*))([\\+\\-\\~])?(/[0-9]+)?)(.*)");

    public static String getHierarchySeparator() {
        return CommonEditorSettings.getHierarchySeparator();
    }

    public static String getFlatNameSeparator() {
        return CommonEditorSettings.getFlatNameSeparator();
    }

    public static String hierarchicalToFlatName(String reference) {
        return hierarchicalToFlatName(reference, getFlatNameSeparator(), true);
    }

    private static String hierarchicalToFlatName(String reference, String flatNameSeparator, boolean suppressLeadingSeparator) {
        if (flatNameSeparator == null) {
            flatNameSeparator = getFlatNameSeparator();
        }

        // Do not work with implicit places(?)
        if (reference.startsWith("<")) {
            return reference;
        }
        String ret = "";
        // In this version the first separator is suppressed
        if (!suppressLeadingSeparator && reference.startsWith(getHierarchySeparator())) {
            ret = flatNameSeparator;
        }

        String head = getReferenceHead(reference);
        String tail = getReferenceTail(reference);
        if (tail.isEmpty()) {
            return ret+head;
        }
        return ret + head + hierarchicalToFlatName(tail, flatNameSeparator, false);
    }

    public static String flatToHierarchicalName(String flatName) {
        return flatToHierarchicalName(flatName, getFlatNameSeparator());
    }

    private static String flatToHierarchicalName(String reference, String flatSeparator) {
        if (flatSeparator == null) {
            flatSeparator = getFlatNameSeparator();
        }
        return reference.replaceAll(flatSeparator, getHierarchySeparator());
    }

    public static void splitReference(String reference, LinkedList<String> path) {
        if (reference.isEmpty()) return;

        Matcher matcher = hPattern.matcher(reference);
        if (matcher.find()) {
            String str = matcher.group(2);
            str=str.replace("'", "");
            path.add(str);
            splitReference(matcher.group(9), path);
        }
    }

    public static String getParentReference(String reference) {
        // legacy reference support
        if (Identifier.isNumber(reference)) return "";

        LinkedList<String> path = new LinkedList<String>();
        splitReference(reference, path);

        String ret = "";
        for (int i = 0; i < path.size() - 1; i++) {
            ret += path.get(i);
            if (i < path.size() - 2) {
                ret += getHierarchySeparator();
            }
        }
        return ret;
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
        if (Identifier.isNumber(reference)) return reference;

        Matcher matcher = hPattern.matcher(reference);
        if (matcher.find()) {
            String head = matcher.group(2);
            head = head.replace("'", "");
            return head;
        }
        return null;
    }

    public static String getReferenceTail(String reference) {
        // legacy reference support
        if (Identifier.isNumber(reference)) return "";

        Matcher matcher = hPattern.matcher(reference);
        if (matcher.find()) {
            String tail = matcher.group(9);
            return tail;
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
            vis = (VisualComponent) Hierarchy.getNearestAncestor(visualContainer, VisualComponent.class);
        }

        // Get appropriate math container, it will be the target container for the math model.
        MathModel mmodel = visualModel.getMathModel();
        Container mathTargetContainer = mmodel.getRoot();
        if (vis != null) {
            mathTargetContainer = (Container) vis.getReferencedComponent();
        }
        return mathTargetContainer;
    }

    private static void copyPageStructure(VisualModel srcModel, Container srcContainer,    VisualModel dstModel, Container dstContainer) {
        HashMap<Container, Container> toProcess = new HashMap<Container, Container>();
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
        HashMap<String, Container> result = new HashMap<String, Container>();
        Container root = model.getRoot();
        result.put("", root);
        for (VisualPage page: Hierarchy.getDescendantsOfType(root, VisualPage.class)) {
            String ref = model.getNodeMathReference(page);
            result.put(ref, page);
        }
        return result;
    }
}
