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

package org.workcraft.testing.dom.visual;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.util.Hierarchy;

public class HierarchyHelperTests {

	static class MockHierarchyNode implements Node
	{
		private Node parent;
		public MockHierarchyNode(Node parent) {
			this.parent = parent;
		}
		@Override
		public Collection<Node> getChildren() {
			throw new RuntimeException("not implemented");
		}
		@Override
		public Node getParent() {
			return parent;
		}
		@Override
		public void setParent(Node parent) {
			throw new RuntimeException("not implemented");
		}
	}

	@Test
	public void TestGetPath()
	{
		MockHierarchyNode node1 = new MockHierarchyNode(null);
		MockHierarchyNode node2 = new MockHierarchyNode(node1);
		MockHierarchyNode node3 = new MockHierarchyNode(node2);
		MockHierarchyNode node3_ = new MockHierarchyNode(node2);

		Assert.assertArrayEquals(new Node[]{node1}, Hierarchy.getPath(node1));
		Assert.assertArrayEquals(new Node[]{node1, node2}, Hierarchy.getPath(node2));
		Assert.assertArrayEquals(new Node[]{node1, node2, node3}, Hierarchy.getPath(node3));
		Assert.assertArrayEquals(new Node[]{node1, node2, node3_}, Hierarchy.getPath(node3_));
	}
}
