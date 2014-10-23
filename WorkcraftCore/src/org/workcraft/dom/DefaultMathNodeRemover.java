/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

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
		else {
//			throw new RuntimeException("The element "+node.toString()+" was added before already!");
			// TODO: why would we increase the counter more than once? which objects use that?
			// TODO: with the counter, the reparenting in GroupImplementation still needs to be fixed not to cause double increase of the counter
//			refCount.put(node, refCount.get(node)+1);
		}
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