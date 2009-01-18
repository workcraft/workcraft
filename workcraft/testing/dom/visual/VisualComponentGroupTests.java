package org.workcraft.testing.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.Test;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualComponentGroup;
import org.workcraft.dom.visual.VisualNode;

import junit.framework.Assert;

public class VisualComponentGroupTests {

	private class SquareNode extends VisualComponent
	{
		Rectangle2D.Double rectOuter;
		Rectangle2D.Double rectInner;
		int resultToReturn;
		public SquareNode(VisualComponentGroup parent, Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
			super(null, parent);
			this.rectOuter = rectOuter;
			this.rectInner = rectInner;
		}

		public SquareNode(VisualComponentGroup parent, Rectangle2D.Double rect) {
			this(parent, rect, rect);
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

		@Override
		public String toString() {
			return rectInner.toString();
		}

		@Override
		public Rectangle2D getBoundingBoxInLocalSpace() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
			// TODO Auto-generated method stub
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

	private SquareNode getSquareNode(VisualNode parent, double x, double y)
	{
		return new SquareNode(null, new Rectangle2D.Double(x, y, 1, 1));
	}

	@Test
	public void TestHitSubGroup()
	{
		VisualComponentGroup root = new VisualComponentGroup(null);

		VisualComponentGroup node1 = new VisualComponentGroup(root);
		VisualComponentGroup node2 = new VisualComponentGroup(root);
		root.add(node1);
		root.add((VisualNode)node2);
		node1.add(getSquareNode(node1, 0, 0));
		node2.add(getSquareNode(node2, 1, 1));
		Assert.assertEquals(node2, root.hitNode(new Point2D.Double(1.5, 1.5)));
		Assert.assertEquals(node1, root.hitNode(new Point2D.Double(0.5, 0.5)));
	}
}
