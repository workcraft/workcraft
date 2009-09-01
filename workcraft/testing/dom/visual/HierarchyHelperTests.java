package org.workcraft.testing.dom.visual;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.HierarchyHelper;

public class HierarchyHelperTests {

	static class MockHierarchyNode implements HierarchyNode
	{
		private HierarchyNode parent;
		public MockHierarchyNode(HierarchyNode parent) {
			this.parent = parent;
		}
		@Override
		public Collection<HierarchyNode> getChildren() {
			throw new RuntimeException("not implemented");
		}
		@Override
		public HierarchyNode getParent() {
			return parent;
		}
		@Override
		public void setParent(HierarchyNode parent) {
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

		Assert.assertArrayEquals(new HierarchyNode[]{node1}, HierarchyHelper.getPath(node1));
		Assert.assertArrayEquals(new HierarchyNode[]{node1, node2}, HierarchyHelper.getPath(node2));
		Assert.assertArrayEquals(new HierarchyNode[]{node1, node2, node3}, HierarchyHelper.getPath(node3));
		Assert.assertArrayEquals(new HierarchyNode[]{node1, node2, node3_}, HierarchyHelper.getPath(node3_));
	}
}
