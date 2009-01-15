package org.workcraft.testing.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.Test;
import org.workcraft.dom.visual.VisualComponentGroup;
import org.workcraft.dom.visual.VisualNode;

import junit.framework.Assert;

public class VisualComponentGroupTests {

	private class SquareNode extends VisualNode
	{
		Rectangle2D.Double rectOuter;
		Rectangle2D.Double rectInner;
		int resultToReturn;
		public SquareNode(VisualComponentGroup parent, Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
			super(parent);
			this.rectOuter = rectOuter;
			this.rectInner = rectInner;
		}

		@Override
		public void draw(Graphics2D g) {
			throw new RuntimeException("Not implemented");
		}

		@Override
		public Rectangle2D getBoundingBoxInParentSpace() {
			return rectOuter;
		}

		@Override
		public int hitTestInParentSpace(Point2D pointInParentSpace) {
			if(rectInner.contains(pointInParentSpace))
				return 1;
			return 0;
		}
	}

	@Test
	public void TestHitNode()
	{
		VisualComponentGroup group = new VisualComponentGroup(null);

		Rectangle2D.Double r1 = new Rectangle2D.Double();
		Rectangle2D.Double r1_ = new Rectangle2D.Double();
		Rectangle2D.Double r2 = new Rectangle2D.Double();
		Rectangle2D.Double r2_ = new Rectangle2D.Double();
		Rectangle2D.Double r3 = new Rectangle2D.Double();
		Rectangle2D.Double r3_ = new Rectangle2D.Double();

		r1.setRect(0, 0, 2, 2);
		r1_.setRect(0.1, 0.1, 1.8, 1.8);
		r2.setRect(0.5, 0.5, 2, 2);
		r2_.setRect(0.6, 0.6, 1.8, 1.8);
		r3.setRect(1, 1, 2, 2);
		r3_.setRect(1.1, 1.1, 1.8, 1.8);

		VisualNode node1 = new SquareNode(group, r1, r1_);
		VisualNode node2 = new SquareNode(group, r2, r2_);
		VisualNode node3 = new SquareNode(group, r3, r3_);

		Assert.assertNull(group.getBoundingBoxInLocalSpace());

		group.add(node1);
		group.add(node2);
		group.add(node3);
		Assert.assertNull(group.hitNode(new Point2D.Double(-1, -1)));
		Assert.assertNull(group.hitNode(new Point2D.Double(10, 10)));
		Assert.assertNull(group.hitNode(new Point2D.Double(0.05, 0.05)));
		Assert.assertEquals(node1, group.hitNode(new Point2D.Double(0.15, 0.5)));
		Assert.assertEquals(node1, group.hitNode(new Point2D.Double(0.55, 0.55)));
		Assert.assertEquals(node2, group.hitNode(new Point2D.Double(0.65, 0.65)));
		Assert.assertEquals(node3, group.hitNode(new Point2D.Double(1.95, 1.95)));
		Assert.assertEquals(node3, group.hitNode(new Point2D.Double(2.35, 1.35)));
		Assert.assertEquals(node3, group.hitNode(new Point2D.Double(2.45, 1.45)));
		Assert.assertEquals(node3, group.hitNode(new Point2D.Double(2.85, 2.85)));
		Assert.assertNull(group.hitNode(new Point2D.Double(2.95, 2.95)));
	}
}
