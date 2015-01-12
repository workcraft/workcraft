package org.workcraft.gui.graph.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;

public class DefaultModelConverter<TSrcModel extends VisualModel, TDstModel extends VisualModel> {
	final private TSrcModel srcModel;
	final private TDstModel dstModel;
	final HashMap<VisualNode, VisualNode> srcToDstNodes;

	public DefaultModelConverter(TSrcModel srcModel, TDstModel dstModel) {
		this.srcModel = srcModel;
		this.dstModel = dstModel;
		this.srcToDstNodes = new HashMap<>();
		convert();
	}

	public TSrcModel getSrcModel() {
		return srcModel;
	}

	public TDstModel getDstModel() {
		return dstModel;
	}

	private void putSrcToDstNode(VisualNode srcNode, VisualNode dstNode) {
		srcToDstNodes.put(srcNode, dstNode);
	}

	private VisualNode getSrcToDstNode(VisualNode srcNode) {
		return srcToDstNodes.get(srcNode);
	}

	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = new HashMap<>();
		result.put(CommentNode.class, CommentNode.class);
		return result;
	}

	public String convertNodeName(String srcName, Container container) {
		return srcName;
	}

	public void afterConversion() {
	}

	public void beforeConversion() {
	}

	private Class<? extends VisualComponent> getVisualComponentClass(Class<? extends MathNode> mathNodeClass) {
		VisualClass visualClassAnnotation = mathNodeClass.getAnnotation(VisualClass.class);
		Class<?> visualClass = visualClassAnnotation.value();
		Class<? extends VisualComponent> visualComponentClass = visualClass.asSubclass(VisualComponent.class);
		return visualComponentClass;
	}

	private void convert() {
		beforeConversion();
		convertPages();
		convertComponents();
		convertGroups();
		// Connections must be converted the last as their shapes change with node relocation.
		convertConnections();
		afterConversion();
	}

	private void convertPages() {
		NamespaceHelper.copyPageStructure(srcModel, dstModel);
		HashMap<String, Container> refToPage = NamespaceHelper.getRefToPageMapping(dstModel);
		for (VisualPage srcPage: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualPage.class)) {
			String ref = srcModel.getNodeMathReference(srcPage);
			Container dstContainer = refToPage.get(ref);
			if (dstContainer instanceof VisualPage) {
				VisualPage dstPage = (VisualPage)dstContainer;
				dstPage.copyStyle(srcPage);
				putSrcToDstNode(srcPage, dstPage);
			}
		}
	}

	private void convertComponents() {
		HashMap<String, Container> refToPage = NamespaceHelper.getRefToPageMapping(dstModel);
		Map<Class<? extends MathNode>, Class<? extends MathNode>> clsMap = getClassMap();
		for (Class<? extends MathNode> srcMathNodeClass: clsMap.keySet()) {
			Class<? extends MathNode> dstMathNodeClass = clsMap.get(srcMathNodeClass);
			Class<? extends VisualComponent> srcVisualComponentClass = getVisualComponentClass(srcMathNodeClass);
			Class<? extends VisualComponent> dstVisualComponentClass = getVisualComponentClass(dstMathNodeClass);
			for(VisualComponent srcComponent: Hierarchy.getDescendantsOfType(srcModel.getRoot(), srcVisualComponentClass)) {
				String srcRef = srcModel.getNodeMathReference(srcComponent);
				if (srcRef != null) {
					String path = NamespaceHelper.getParentReference(srcRef);
					Container container = refToPage.get(path);
					String srcName = NamespaceHelper.getNameFromReference(srcRef);
					String dstName = convertNodeName(srcName, container);
					Container mathContainer = NamespaceHelper.getMathContainer(dstModel, container);
					MathNode dstMathNode = dstModel.getMathModel().createNode(dstName, mathContainer, dstMathNodeClass);
					VisualComponent dstComponent = dstModel.createComponent(dstMathNode, container, dstVisualComponentClass);
					dstComponent.copyStyle(srcComponent);
					putSrcToDstNode(srcComponent, dstComponent);
				}
			}
		}
	}

	private void convertConnections() {
		for(VisualConnection srcConnection : Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualConnection.class)) {
			VisualComponent srcFirst = srcConnection.getFirst();
			VisualComponent srcSecond = srcConnection.getSecond();
			VisualNode dstFirst = getSrcToDstNode(srcFirst);
			VisualNode dstSecond= getSrcToDstNode(srcSecond);
			if ((dstFirst != null) && (dstSecond != null)) {
				try {
					VisualConnection dstConnection = dstModel.connect(dstFirst, dstSecond);
					dstConnection.copyStyle(srcConnection);
					putSrcToDstNode(srcConnection, dstConnection);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void convertGroups() {
		for(VisualGroup srcGroup: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualGroup.class)) {
			HashSet<Node> dstSelection = new HashSet<>();
			for (Node srcNode: srcGroup.getChildren()) {
				Node dstNode = null;
				if (srcNode instanceof VisualNode) {
					dstNode = getSrcToDstNode((VisualNode)srcNode);
				}
				if (dstNode != null) {
					dstSelection.add(dstNode);
				}
			}
			if ( !dstSelection.isEmpty() ) {
				Container c = Hierarchy.getNearestContainer(dstSelection);
				dstModel.setCurrentLevel(c);
				dstModel.addToSelection(dstSelection);
				VisualGroup dstGroup = dstModel.groupSelection();
				dstModel.selectNone();
				dstGroup.copyStyle(srcGroup);
				putSrcToDstNode(srcGroup, dstGroup);
			}
		}
	}

}
