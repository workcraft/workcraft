package org.workcraft.dom;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesDeletedEvent;

public class DefaultMathNodeRemover extends HierarchySupervisor {
	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesDeletedEvent)
			for (Node node : e.getAffectedNodes())
				if (node instanceof VisualNode)
					for (MathNode mn : ((VisualNode)node).getMathReferences())
						if (mn.getParent() instanceof Container)
							((Container)mn.getParent()).remove(mn);
	}
}