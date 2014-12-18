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
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;

public class DefaultModelConverter<TSrcModel extends VisualModel, TDstModel extends VisualModel> {
	final private TSrcModel srcModel;
	final private TDstModel dstModel;
	final private HashMap<String, Container> pathToDstContainers;
	final HashMap<VisualNode, VisualNode> srcToDstNodes;

	public DefaultModelConverter(TSrcModel srcModel, TDstModel dstModel) {
		this.srcModel = srcModel;
		this.dstModel = dstModel;
		this.pathToDstContainers = NamespaceHelper.copyPageStructure(dstModel, dstModel.getRoot(), srcModel, srcModel.getRoot(), null);
		this.srcToDstNodes = new HashMap<>();
		convertComponents();
		convertConnections();
		convertGroups();
	}

	public TSrcModel getSrcModel() {
		return srcModel;
	}

	public TDstModel getDstModel() {
		return dstModel;
	}

	private void putSrcToDstComponent(VisualNode srcComponent, VisualNode dstComponent) {
		srcToDstNodes.put(srcComponent, dstComponent);
	}

	private VisualNode getSrcToDstComponent(VisualNode srcComponent) {
		return srcToDstNodes.get(srcComponent);
	}

	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = new HashMap<>();
		result.put(CommentNode.class, CommentNode.class);
		return result;
	}

	private Class<? extends VisualComponent> getVisualComponentClass(Class<? extends MathNode> mathNodeClass) {
		VisualClass visualClassAnnotation = mathNodeClass.getAnnotation(VisualClass.class);
		Class<?> visualClass = visualClassAnnotation.value();
		Class<? extends VisualComponent> visualComponentClass = visualClass.asSubclass(VisualComponent.class);
		return visualComponentClass;
	}

	private void convertComponents() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> clsMap = getClassMap();
		for (Class<? extends MathNode> srcMathNodeClass: clsMap.keySet()) {
			Class<? extends MathNode> dstMathNodeClass = clsMap.get(srcMathNodeClass);
			Class<? extends VisualComponent> srcVisualComponentClass = getVisualComponentClass(srcMathNodeClass);
			Class<? extends VisualComponent> dstVisualComponentClass = getVisualComponentClass(dstMathNodeClass);
			for(VisualComponent srcComponent: Hierarchy.getDescendantsOfType(srcModel.getRoot(), srcVisualComponentClass)) {
				String ref = srcModel.getNodeMathReference(srcComponent);
				if (ref != null) {
					String path = NamespaceHelper.getParentReference(ref);
					String name = NamespaceHelper.getNameFromReference(ref);
					Container container = pathToDstContainers.get(path);
					Container mathContainer = NamespaceHelper.getMathContainer(dstModel, container);
					MathNode dstMathNode = dstModel.getMathModel().createNode(name, mathContainer, dstMathNodeClass);
					VisualComponent dstComponent = dstModel.createComponent(dstMathNode, container, dstVisualComponentClass);
					dstComponent.copyStyle(srcComponent);
					putSrcToDstComponent(srcComponent, dstComponent);
				}
			}
		}
	}

	private void convertConnections() {
		for(VisualConnection srcConnection : Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualConnection.class)) {
			VisualComponent srcFirst = srcConnection.getFirst();
			VisualComponent srcSecond = srcConnection.getSecond();
			VisualNode dstFirst = getSrcToDstComponent(srcFirst);
			VisualNode dstSecond= getSrcToDstComponent(srcSecond);
			if ((dstFirst != null) && (dstSecond != null)) {
				try {
					VisualConnection newConnection = dstModel.connect(dstFirst, dstSecond);
					newConnection.copyStyle(srcConnection);
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
				if (srcNode instanceof VisualComponent) {
					dstNode = getSrcToDstComponent((VisualComponent)srcNode);
				}
				if (dstNode != null) {
					dstSelection.add(dstNode);
					String ref = dstModel.getNodeMathReference(dstNode);
					String path = NamespaceHelper.getParentReference(ref);
					Container container = pathToDstContainers.get(path);
					dstModel.setCurrentLevel(container);
				}
			}
			if ( !dstSelection.isEmpty() ) {
				dstModel.addToSelection(dstSelection);
				VisualGroup dstGroup = dstModel.groupSelection();
				if (dstGroup != null) {
					dstGroup.copyStyle(srcGroup);
					putSrcToDstComponent(srcGroup, dstGroup);
				}
			}
		}
		dstModel.selectNone();
	}

}
