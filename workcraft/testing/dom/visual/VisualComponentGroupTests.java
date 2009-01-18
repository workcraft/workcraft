package org.workcraft.testing.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.junit.Test;
import org.workcraft.dom.visual.VisualComponentGroup;
import org.workcraft.dom.visual.VisualNode;

import junit.framework.Assert;

public class VisualComponentGroupTests {

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

	@Test
	public void TestUngroup()
	{
		VisualComponentGroup root = new VisualComponentGroup(null);

		VisualComponentGroup node1 = new VisualComponentGroup(root);
		root.add(node1);

		node1.setX(10);
		node1.setY(15);

		VisualComponentGroup node2 = new VisualComponentGroup(node1);
		node1.add(node2);

		SquareNode sq1 = getSquareNode(node1, 0, 0);
		node1.add(sq1);
		SquareNode sq2 = getSquareNode(node1, 1, 1);
		node1.add(sq2);
		SquareNode sq3 = getSquareNode(node1, 2, 2);
		node1.add(sq3);

		Assert.assertEquals(sq1, node1.hitNode(new Point2D.Double(0.5, 0.5)));
		Assert.assertEquals(sq2, node1.hitNode(new Point2D.Double(1.5, 1.5)));

		Assert.assertEquals(node1, root.hitNode(new Point2D.Double(10.5, 15.5)));
		Assert.assertEquals(node1, root.hitNode(new Point2D.Double(11.5, 16.5)));
		Assert.assertEquals(null, root.hitNode(new Point2D.Double(10.5, 16.5)));

		Iterable<VisualNode> unGroup = node1.unGroup();
		ArrayList<VisualNode> list = new ArrayList<VisualNode>();
		for(VisualNode node: unGroup)
			list.add(node);

		Assert.assertEquals(4, list.size());
		Assert.assertTrue(list.contains(sq1));
		Assert.assertTrue(list.contains(sq2));
		Assert.assertTrue(list.contains(sq3));
		Assert.assertTrue(list.contains(node2));

		Assert.assertTrue(list.indexOf(sq2) > list.indexOf(sq1));
		Assert.assertTrue(list.indexOf(sq3) > list.indexOf(sq2));

		Assert.assertNull(node1.hitNode(new Point2D.Double(0.5, 0.5)));
		Assert.assertNull(node1.hitNode(new Point2D.Double(1.5, 1.5)));

		Assert.assertNull(root.hitNode(new Point2D.Double(10.5, 15.5)));
		Assert.assertNull(root.hitNode(new Point2D.Double(11.5, 16.5)));
		Assert.assertNull(root.hitNode(new Point2D.Double(10.5, 16.5)));

		for(VisualNode node: unGroup)
			root.add(node);

		Assert.assertNull(node1.hitNode(new Point2D.Double(0.5, 0.5)));
		Assert.assertNull(node1.hitNode(new Point2D.Double(1.5, 1.5)));

		Assert.assertEquals(sq1, root.hitNode(new Point2D.Double(10.5, 15.5)));
		Assert.assertEquals(sq2, root.hitNode(new Point2D.Double(11.5, 16.5)));
		Assert.assertEquals(null, root.hitNode(new Point2D.Double(10.5, 16.5)));

	}
}
