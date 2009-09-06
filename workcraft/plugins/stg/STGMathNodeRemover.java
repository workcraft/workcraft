/**
 *
 */
package org.workcraft.plugins.stg;

import java.util.HashSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesDeletedEvent;

class STGMathNodeRemover extends HierarchySupervisor {
	private HashSet<Node> keepMathNodesFor = new HashSet<Node>();

	public void keepMathNodesFor (Node node) {
		keepMathNodesFor.add(node);
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesDeletedEvent)
			for (Node node : e.getAffectedNodes()) {
				if (keepMathNodesFor.contains(node)) {
					keepMathNodesFor.remove(node);
					continue;
				}
				if (node instanceof VisualNode)
					for (MathNode mn : ((VisualNode)node).getMathReferences())
						if (mn.getParent() instanceof Container)
							((Container)mn.getParent()).remove(mn);
			}
	}
}