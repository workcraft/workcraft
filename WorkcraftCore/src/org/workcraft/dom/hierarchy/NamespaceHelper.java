package org.workcraft.dom.hierarchy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.util.Identifier;

public class NamespaceHelper {
	final static String quoteType = "'";
	final public static String hierarchySeparator = "/";
	final public static String hierarchyRoot = hierarchySeparator;
	final public static String flatNameSeparator = "__";

	// TODO: make it work with the embedded ' characters
	private static Pattern hPattern = Pattern.compile("(/)?(((\\'([^\\']+)\\')|([_A-Za-z][_A-Za-z0-9]*))([\\+\\-\\~])?(/[0-9]+)?)(.*)");

	public static String hierarchicalToFlatName(String reference) {
		return hierarchicalToFlatName(reference, flatNameSeparator, true);
	}

	private static String hierarchicalToFlatName(String reference, String flatSeparator, boolean suppressLeadingSeparator) {
		if (flatSeparator==null) flatSeparator=flatNameSeparator;

		// Do not work with implicit places(?)
		if (reference.startsWith("<")) {
			return reference;
		}
		String ret = "";
		// In this version the first separator is suppressed
		if (!suppressLeadingSeparator && reference.startsWith(hierarchySeparator)) {
			ret=flatSeparator;
		}

		String head = getReferenceHead(reference);
		String tail = getReferenceTail(reference);
		if (tail.equals("")) {
			return ret+head;
		}
		return ret + head + hierarchicalToFlatName(tail, flatSeparator, false);
	}

	public static String flatToHierarchicalName(String flatName) {
		return flatToHierarchicalName(flatName, flatNameSeparator);
	}

	private static String flatToHierarchicalName(String reference, String flatSeparator) {
		if (flatSeparator==null) {
			flatSeparator = flatNameSeparator;
		}
		return reference.replaceAll(flatSeparator, hierarchySeparator);
	}

	public static void splitReference(String reference, LinkedList<String> path) {
		if (reference.equals("")) return;

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
				ret += hierarchySeparator;
			}
		}
		return ret;
	}

	public static String getReferencePath(String reference) {
		String ret = getParentReference(reference);
		if (ret.length() > 0) {
			ret += hierarchySeparator;
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

	public static String getNameFromReference(String reference) {
		String head = getReferenceHead(reference);
		String tail = getReferenceTail(reference);
		if (tail.equals("")) {
			return head;
		}
		return getNameFromReference(tail);
	}

	public static HashMap<String, Container> copyPageStructure(VisualModel targetModel, Container targetContainer,
			VisualModel sourceModel, Container sourceContainer, HashMap<String, Container> createdPageContainers) {
		if (createdPageContainers == null) {
			createdPageContainers = new HashMap<String, Container>();
		}
		createdPageContainers.put("", targetModel.getRoot());
		HashMap<Container, Container> toProcess = new HashMap<Container, Container>();
		for (Node vn: sourceContainer.getChildren()) {
			if (vn instanceof VisualPage) {
				VisualPage vp = (VisualPage)vn;
				String name = sourceModel.getMathName(vp);

				PageNode np2 = new PageNode();
				VisualPage vp2 = new VisualPage(np2);
				targetContainer.add(vp2);
				vp2.copyStyle(vp);

				AbstractVisualModel.getMathContainer(targetModel, targetContainer).add(np2);
				targetModel.getMathModel().setName(np2, name);
				String ref = targetModel.getNodeMathReference(vp2);
				createdPageContainers.put(ref, vp2);

				toProcess.put(vp, vp2);
			}
		}

		for (Entry<Container, Container> en: toProcess.entrySet()) {
			copyPageStructure(targetModel, en.getValue(), sourceModel, en.getKey(), createdPageContainers);
		}
		return createdPageContainers;
	}

}
