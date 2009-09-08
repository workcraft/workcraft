package org.workcraft.dom;

import java.util.HashMap;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;

public class DefaultMathNodeRemover extends HierarchySupervisor {
	private HashMap<MathNode, Integer> refCount = new HashMap<MathNode, Integer>();

	private void incRef (MathNode node) {
		if (refCount.get(node) == null)
			refCount.put(node, 1);
		else
			refCount.put(node, refCount.get(node)+1);
	}

	private void decRef (MathNode node) {
		Integer refs = refCount.get(node)-1;
		if (refs == 0) {
			refCount.remove(node);
			if (node.getParent() instanceof Container)
				((Container)node.getParent()).remove(node);
		} else
			refCount.put(node, refs);
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesDeletedEvent)
			for (Node node : e.getAffectedNodes())
				nodeRemoved(node);

		if (e instanceof NodesAddedEvent)
			for (Node node : e.getAffectedNodes())
				nodeAdded(node);
	}

	private void nodeAdded(Node node) {
		if (node instanceof VisualNode)
			for (MathNode mn : ((VisualNode)node).getMathReferences())
				incRef(mn);

		for (Node n : node.getChildren())
			nodeAdded(n);
	}

	private void nodeRemoved(Node node) {
		if (node instanceof VisualNode)
			for (MathNode mn : ((VisualNode)node).getMathReferences())
				decRef(mn);

		for (Node n : node.getChildren())
			nodeRemoved(n);
	}
}