package org.workcraft.dom;

import java.util.HashMap;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;

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
			// System.out.println ( "Math node " + node + " is no longer referenced to, deleting");
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
		if (node instanceof DependentNode)
			for (MathNode mn : ((DependentNode)node).getMathReferences())
				incRef(mn);

		for (Node n : node.getChildren())
			nodeAdded(n);
	}

	private void nodeRemoved(Node node) {
		if (node instanceof DependentNode)
			for (MathNode mn : ((DependentNode)node).getMathReferences())
				decRef(mn);

		for (Node n : node.getChildren())
			nodeRemoved(n);
	}
}