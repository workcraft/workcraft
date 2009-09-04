package org.workcraft.testing.dom.visual;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Touchable;

public class HitmanTests {
	class DummyNode implements HierarchyNode
	{
		Collection<HierarchyNode> children;
		public DummyNode()
		{
			children = Collections.emptyList();
		}
		public DummyNode(HierarchyNode[] children)
		{
			this.children = new ArrayList<HierarchyNode>(Arrays.asList(children));
		}
		public DummyNode(Collection<HierarchyNode> children)
		{
			this.children = children;
		}

		public Collection<HierarchyNode> getChildren() {
			return children;
		}

		public HierarchyNode getParent() {
			throw new RuntimeException("Not Implemented");
		}

		public void setParent(HierarchyNode parent) {
			throw new RuntimeException("Not Implemented");
		}
	}

	class HitableNode extends DummyNode implements Touchable
	{
		public Rectangle2D getBoundingBox() {
			return new Rectangle2D.Double(0, 0, 1, 1);
		}

		public boolean hitTest(Point2D point) {
			return true;
		}
	}

	@Test
	public void TestHitDeepestSkipNulls()
	{
		final HitableNode toHit = new HitableNode();
		HierarchyNode node = new DummyNode(
			new HierarchyNode[]{
					new DummyNode(new HierarchyNode[]{ toHit }),
					new DummyNode(),
			}
		);
		assertSame(toHit, HitMan.hitDeepestNodeOfType(new Point2D.Double(0.5, 0.5), node, HitableNode.class));
	}
}
