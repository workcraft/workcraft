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

import static org.workcraft.testing.dom.visual.Tools.createComponent;
import static org.workcraft.testing.dom.visual.Tools.createConnection;
import static org.workcraft.testing.dom.visual.Tools.createGroup;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.VisualModelInstantiationException;

public class VisualModelTests {

	private class MockMathModel extends AbstractModel {
		public MockMathModel() {
			super(new MathGroup());
		}
	}

	private class MockConcreteVisualModel extends AbstractVisualModel {

		public MockConcreteVisualModel() {
			super(new MockMathModel());
		}

		@Override
		public void validateConnection(Node first, Node second)
				throws InvalidConnectionException {
			// TODO Auto-generated method stub

		}

		@Override
		public void connect(Node first, Node second)
				throws InvalidConnectionException {
		}
	}

	@Test
	public void TestGroupWithEmptySelection()
			throws VisualModelInstantiationException {
		VisualModel model = new MockConcreteVisualModel();

		model.getCurrentLevel().add(new VisualGroup());

		VisualNode[] old = model.getCurrentLevel().getChildren().toArray(new VisualNode[0]);
		Assert.assertEquals(1, old.length);
		model.groupSelection();
		VisualNode[] _new = model.getCurrentLevel().getChildren().toArray(new VisualNode[0]);
		Assert.assertEquals(1, _new.length);
		Assert.assertEquals(old[0], _new[0]);
	}

	VisualNode[] findMissing(VisualNode[] oldNodes, VisualNode[] newNodes) {
		VisualNode[] diffs = new VisualNode[oldNodes.length
				- (newNodes.length - 1)];

		int dc = 0;
		for (int oc = 0; oc < oldNodes.length; oc++) {
			if (oldNodes[oc] == newNodes[oc - dc])
				continue;
			diffs[dc] = oldNodes[oc];
			dc++;
		}
		if (dc != diffs.length)
			throw new RuntimeException("incorrect arrays!");

		return diffs;
	}

	public void TestGroup(VisualModel model, VisualNode[] toGroup) {
		model.selectNone();
		for (VisualNode node : toGroup) {
			model.addToSelection(node);
		}

		VisualNode[] old = model.getCurrentLevel().getChildren().toArray(new VisualNode[0]);

		model.groupSelection();

		VisualNode[] _new = model.getCurrentLevel().getChildren().toArray(new VisualNode[0]);

		VisualNode[] diff = findMissing(old, _new);

		ArrayList<VisualNode> missingList = new ArrayList<VisualNode>();

		for (VisualNode node : diff)
			missingList.add(node);

		Assert.assertEquals(toGroup.length, missingList.size());
		for (VisualNode node : toGroup)
			Assert.assertTrue(missingList.contains(node));

		ArrayList<VisualNode> oldList = new ArrayList<VisualNode>();
		for (VisualNode node : old)
			oldList.add(node);

		VisualGroup newGroup = (VisualGroup) _new[_new.length - 1];
		Assert.assertFalse(oldList.contains(newGroup));
		Assert.assertTrue(newGroup instanceof VisualGroup);

		ArrayList<Node> newNodeList = new ArrayList<Node>();
		for (Node node : newGroup.getChildren())
			newNodeList.add(node);

		Assert.assertEquals(toGroup.length, newNodeList.size());
		for (VisualNode node : toGroup)
			Assert.assertTrue(newNodeList.contains(node));
	}

	@Test
	public void TestGroup2Items() throws VisualModelInstantiationException {
		VisualModel model = new MockConcreteVisualModel();

		Container root = model.getCurrentLevel();
		VisualGroup node1 = createGroup(root);
		VisualGroup node2 = createGroup(root);

		TestGroup(model, new VisualNode[] { node1, node2 });
	}

	@Test
	public void TestGroup1Item() throws VisualModelInstantiationException {
		VisualModel model = createModel();

		Container root = model.getCurrentLevel();
		VisualGroup node1 = createGroup(root);

		model.addToSelection(node1);
		model.groupSelection();
		Assert.assertEquals(1, root.getChildren().toArray(new VisualNode[0]).length);
		Assert.assertEquals(node1, root.getChildren().toArray(new VisualNode[0])[0]);
		Assert.assertEquals(0, node1.getChildren().toArray(new VisualNode[0]).length);
	}

	@Test
	public void TestGroup5Items() throws VisualModelInstantiationException {
		VisualModel model = createModel();

		Container root = model.getCurrentLevel();
		VisualGroup node1 = new VisualGroup();
		VisualGroup node2 = new VisualGroup();
		VisualGroup node3 = new VisualGroup();
		VisualGroup node4 = new VisualGroup();
		SquareNode sq1 = new SquareNode(root,
				new Rectangle2D.Double(0, 0, 1, 1));
		SquareNode sq2 = new SquareNode(root,
				new Rectangle2D.Double(0, 0, 1, 1));
		SquareNode sq3 = new SquareNode(root,
				new Rectangle2D.Double(0, 0, 1, 1));
		SquareNode sq4 = new SquareNode(root,
				new Rectangle2D.Double(0, 0, 1, 1));
		SquareNode sq5 = new SquareNode(root,
				new Rectangle2D.Double(0, 0, 1, 1));

		root.add(node1);
		root.add(node2);
		root.add(node3);
		root.add(node4);
		root.add(sq1);
		root.add(sq2);
		root.add(sq3);
		root.add(sq4);
		root.add(sq5);

		TestGroup(model, new VisualNode[] { node1, node3, sq1, sq5 });
	}

	@Test
	public void TestUngroupRoot() throws VisualModelInstantiationException {
		VisualModel model = new MockConcreteVisualModel();

		Container root = model.getCurrentLevel();

		VisualGroup node1 = new VisualGroup();
		VisualGroup node2 = new VisualGroup();
		VisualGroup node3 = new VisualGroup();
		VisualGroup node4 = new VisualGroup();
		VisualGroup node5 = new VisualGroup();

		node2.add(node3);
		node1.add(node2);
		node1.add(node4);
		root.add(node1);
		root.add(node5);

		model.addToSelection(node1);
		model.ungroupSelection();

		VisualNode[] newList = root.getChildren().toArray(new VisualNode[0]);

		Assert.assertEquals(3, newList.length);
		Assert.assertSame(node5, newList[0]);
		Assert.assertSame(node2, newList[1]);
		Assert.assertSame(node4, newList[2]);

		VisualNode[] n2Children = node2.getChildren().toArray(new VisualNode[0]);
		Assert.assertEquals(1, n2Children.length);
		Assert.assertSame(node3, n2Children[0]);
	}

	@Test
	public void TestUngroupNonRoot() throws VisualModelInstantiationException {
		VisualModel model = new MockConcreteVisualModel();

		Container root = model.getCurrentLevel();

		VisualGroup node1 = new VisualGroup();
		VisualGroup node2 = new VisualGroup();
		VisualGroup node3 = new VisualGroup();
		VisualGroup node4 = new VisualGroup();
		VisualGroup node5 = new VisualGroup();

		node2.add(node3);
		node1.add(node2);
		node1.add(node4);
		root.add(node1);
		root.add(node5);

		model.setCurrentLevel(node1);

		model.addToSelection(node2);
		model.ungroupSelection();

		VisualNode[] newList = root.getChildren().toArray(new VisualNode[0]);

		Assert.assertEquals(2, newList.length);
		Assert.assertSame(node1, newList[0]);
		Assert.assertSame(node5, newList[1]);

		VisualNode[] n1Children = node1.getChildren().toArray(new VisualNode[0]);
		Assert.assertEquals(2, n1Children.length);
		Assert.assertSame(node4, n1Children[0]);
		Assert.assertSame(node3, n1Children[1]);
	}

	@Test
	public void TestUngroupEmpty() throws VisualModelInstantiationException {
		VisualModel model = new MockConcreteVisualModel();

		Container root = model.getCurrentLevel();

		VisualGroup node1 = new VisualGroup();
		VisualGroup node2 = new VisualGroup();

		root.add(node1);
		root.add(node2);

		model.addToSelection(node2);
		model.ungroupSelection();

		VisualNode[] newList = root.getChildren().toArray(new VisualNode[0]);

		Assert.assertEquals(1, newList.length);
		Assert.assertSame(node1, newList[0]);

		Assert.assertEquals(0, node1.getChildren().toArray(new VisualNode[0]).length);
	}

	@Test
	public void TestUngroupTwoGroups() throws VisualModelInstantiationException {
		VisualModel model = new MockConcreteVisualModel();

		Container root = model.getCurrentLevel();

		VisualGroup node1 = new VisualGroup();
		VisualGroup node2 = new VisualGroup();
		VisualGroup node3 = new VisualGroup();
		VisualGroup node4 = new VisualGroup();

		VisualGroup node1c = new VisualGroup();
		VisualGroup node2c = new VisualGroup();
		VisualGroup node3c = new VisualGroup();
		VisualGroup node4c = new VisualGroup();

		root.add(node1);
		root.add(node2);
		root.add(node3);
		root.add(node4);

		node1.add(node1c);
		node2.add(node2c);
		node3.add(node3c);
		node4.add(node4c);

		model.addToSelection(node4);
		model.addToSelection(node3);
		model.addToSelection(node1);
		model.addToSelection(node2);
		model.ungroupSelection();

		VisualNode[] newList = root.getChildren().toArray(new VisualNode[0]);

		Assert.assertEquals(4, newList.length);
		Assert.assertSame(node1c, newList[0]);
		Assert.assertSame(node2c, newList[1]);
		Assert.assertSame(node3c, newList[2]);
		Assert.assertSame(node4c, newList[3]);

		Assert.assertEquals(0, node1.getChildren().toArray(new VisualNode[0]).length);
		Assert.assertEquals(0, node2.getChildren().toArray(new VisualNode[0]).length);
		Assert.assertEquals(0, node3.getChildren().toArray(new VisualNode[0]).length);
		Assert.assertEquals(0, node4.getChildren().toArray(new VisualNode[0]).length);
	}

	@Test
	public void testGrouping_AutoGroupConnections() {
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualComponent c1 = createComponent(root);
		VisualComponent c2 = createComponent(root);
		VisualConnection con = createConnection(c1, c2, root);

		model.addToSelection(c1);
		model.addToSelection(c2);
		model.groupSelection();

		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {c1, c2, con}) },
				root.getChildren().toArray(new VisualNode[0]));
	}

	@Test
	public void testGrouping_AutoGroupConnectionsIgnoreSelection() {
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualComponent c1 = createComponent(root);
		VisualComponent c2 = createComponent(root);
		VisualConnection con = createConnection(c1, c2, root);

		model.addToSelection(con);
		model.addToSelection(c1);
		model.addToSelection(c2);
		model.groupSelection();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {c1, c2, con}) },
				root.getChildren().toArray(new VisualNode[0]));
	}
	@Test
	public void testGrouping_AutoGroupTwoConnections() {
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualComponent c1 = createComponent(root);
		VisualComponent c2 = createComponent(root);
		VisualConnection con1 = createConnection(c1, c2, root);
		VisualConnection con2 = createConnection(c1, c2, root);

		model.addToSelection(con1);
		model.addToSelection(c2);
		model.addToSelection(c1);
		model.addToSelection(con2);
		model.groupSelection();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {c1, c2, con1, con2}) },
				root.getChildren().toArray(new VisualNode[0]));
	}

	@Test
	public void testGrouping_AutoGroupConnectionsPointingDeep() {
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualGroup node1 = createGroup(root);
		VisualComponent c1 = createComponent(node1);
		VisualComponent c2 = createComponent(root);
		VisualConnection con = createConnection(c1, c2, root);

		model.addToSelection(node1);
		model.addToSelection(c2);
		model.groupSelection();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {node1, c2, con}) },
				root.getChildren().toArray(new VisualNode[0]));
	}

	@Test
	public void testGrouping_DontGroupConnectionsSimple() {
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualComponent c1 = createComponent(root);
		VisualComponent c2 = createComponent(root);
		VisualComponent c3 = createComponent(root);
		VisualConnection con1 = createConnection(c1, c2, root);
		VisualConnection con2 = createConnection(c2, c3, root);

		model.addToSelection(con1);
		model.addToSelection(con2);
		model.groupSelection();

		Assert.assertArrayEquals(new VisualNode[] { c1, c2, c3, con1, con2 },
				root.getChildren().toArray(new VisualNode[0]));
	}

	class GroupNodeEqualityTest {
		private VisualNode[] expected;

		public GroupNodeEqualityTest(VisualNode[] expected) {
			this.expected = expected;
		}

		public void assertEquals(Node node) {
			Assert.assertTrue("Should be a visual group", node instanceof VisualGroup);
			VisualGroup group = (VisualGroup) node;
			VisualNode[] their = group.getChildren().toArray(new VisualNode[0]);;
			Assert.assertArrayEquals(expected, their);
		}

		public boolean equals(Object obj)
		{
			Assert.assertTrue("Should be a visual group", obj instanceof VisualGroup);
			assertEquals((Node)obj);
			return true;
		}
	}

	@Test
	public void testGrouping_DontGroupConnections() {
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualComponent c1 = createComponent(root);
		VisualComponent c2 = createComponent(root);
		VisualComponent c3 = createComponent(root);
		VisualConnection con1 = createConnection(c1, c2, root);

		model.addToSelection(con1);
		model.addToSelection(c2);
		model.addToSelection(c3);
		model.groupSelection();

		Assert.assertArrayEquals(new Object[] { c1, con1,
				new GroupNodeEqualityTest(new VisualNode[] { c2, c3 }) },
				root.getChildren().toArray(new VisualNode[0]));
	}

	@Test
	public void testGrouping_DontCountConnections() {
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualComponent c1 = createComponent(root);
		VisualComponent c2 = createComponent(root);
		VisualConnection con = createConnection(c1, c2, root);

		model.addToSelection(c1);
		model.addToSelection(con);
		model.groupSelection();
		Assert.assertArrayEquals(new VisualNode[] { c1, c2, con }, root
				.getChildren().toArray(new VisualNode[0]));
	}

	private VisualModel createModel() {
		return new MockConcreteVisualModel();
	}

	@Test
	public void TestHitNodeCurrentLevelTransformation()
	{
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualGroup group1 = createGroup(root);
		group1.setX(101);
		SquareNode sq = new SquareNode(group1, new Rectangle2D.Double(0, 0, 1, 1));
		group1.add(sq);

		Assert.assertNull(HitMan.hitTestForSelection(new Point2D.Double(0.5, 0.5), model));
		Assert.assertEquals(group1, HitMan.hitTestForSelection(new Point2D.Double(101.5, 0.5), model));
		model.setCurrentLevel(group1);
		Assert.assertNull(HitMan.hitTestForSelection(new Point2D.Double(0.5, 0.5), model));
		Assert.assertEquals(sq, HitMan.hitTestForSelection(new Point2D.Double(101.5, 0.5), model));
	}

	@Test
	public void TestHitObjectsCurrentLevelTransformation()
	{
		VisualModel model = createModel();

		VisualGroup root = (VisualGroup)model.getRoot();
		VisualGroup group1 = createGroup(root);
		group1.setX(101);
		SquareNode sq = new SquareNode(group1, new Rectangle2D.Double(0, 0, 1, 1));
		group1.add(sq);

		SquareNode sq2 = new SquareNode(root, new Rectangle2D.Double(0, 5, 1, 1));
		root.add(sq2);

		Assert.assertEquals(0, boxHitTest(model,new Rectangle2D.Double(-0.01, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(1, boxHitTest(model,new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(group1, boxHitTest(model,new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).iterator().next());
		Assert.assertEquals(1, boxHitTest(model,new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).size());
		Assert.assertEquals(sq2, boxHitTest(model,new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).iterator().next());
		model.setCurrentLevel(group1);
		Assert.assertEquals(0, boxHitTest(model,new Rectangle2D.Double(-0.01, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(1, boxHitTest(model,new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(sq, boxHitTest(model,new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).iterator().next());
		Assert.assertEquals(0, boxHitTest(model,new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).size());
	}

	private Collection<Node> boxHitTest(VisualModel model, Rectangle2D.Double rect) {
		return model.boxHitTest(new Point2D.Double(rect.getMinX(), rect.getMinY()),
				new Point2D.Double(rect.getMaxX(), rect.getMaxY()));
	}
}
