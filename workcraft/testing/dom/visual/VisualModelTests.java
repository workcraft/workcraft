package org.workcraft.testing.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

public class VisualModelTests {

	private class MockMathModel extends MathModel {

		public MockMathModel() {
			super();
		}

		@Override
		public void validate() throws ModelValidationException {
			return;
		}

		@Override
		public void validateConnection(Connection connection)
				throws InvalidConnectionException {
			return;
		}


	}

	@Test
	public void TestGroupWithEmptySelection()
			throws VisualModelInstantiationException {
		VisualModel model = new VisualModel(new MockMathModel());

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

		ArrayList<HierarchyNode> newNodeList = new ArrayList<HierarchyNode>();
		for (HierarchyNode node : newGroup.getChildren())
			newNodeList.add(node);

		Assert.assertEquals(toGroup.length, newNodeList.size());
		for (VisualNode node : toGroup)
			Assert.assertTrue(newNodeList.contains(node));
	}

	@Test
	public void TestGroup2Items() throws VisualModelInstantiationException {
		VisualModel model = new VisualModel(new MockMathModel());

		VisualGroup root = model.getCurrentLevel();
		VisualGroup node1 = Tools.createGroup(root);
		VisualGroup node2 = Tools.createGroup(root);

		TestGroup(model, new VisualNode[] { node1, node2 });
	}

	@Test
	public void TestGroup1Item() throws VisualModelInstantiationException {
		VisualModel model = createModel();

		VisualGroup root = model.getCurrentLevel();
		VisualGroup node1 = Tools.createGroup(root);

		model.addToSelection(node1);
		model.groupSelection();
		Assert.assertEquals(1, root.getChildren().toArray(new VisualNode[0]).length);
		Assert.assertEquals(node1, root.getChildren().toArray(new VisualNode[0])[0]);
		Assert.assertEquals(0, node1.getChildren().toArray(new VisualNode[0]).length);
	}

	@Test
	public void TestGroup5Items() throws VisualModelInstantiationException {
		VisualModel model = createModel();

		VisualGroup root = model.getCurrentLevel();
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
		VisualModel model = new VisualModel(new MockMathModel());

		VisualGroup root = model.getCurrentLevel();

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
		VisualModel model = new VisualModel(new MockMathModel());

		VisualGroup root = model.getCurrentLevel();

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
		VisualModel model = new VisualModel(new MockMathModel());

		VisualGroup root = model.getCurrentLevel();

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
		VisualModel model = new VisualModel(new MockMathModel());

		VisualGroup root = model.getCurrentLevel();

		VisualGroup node1 = new VisualGroup();
		VisualGroup node2 = new VisualGroup();
		VisualGroup node3 = new VisualGroup();
		VisualGroup node4 = new VisualGroup();

		root.add(node1);
		root.add(node2);

		node1.add(node3);
		node2.add(node4);

		model.addToSelection(node2);
		model.addToSelection(node1);
		model.ungroupSelection();

		VisualNode[] newList = root.getChildren().toArray(new VisualNode[0]);

		Assert.assertEquals(2, newList.length);
		Assert.assertSame(node4, newList[0]);
		Assert.assertSame(node3, newList[1]);

		Assert.assertEquals(0, node3.getChildren().toArray(new VisualNode[0]).length);
		Assert.assertEquals(0, node4.getChildren().toArray(new VisualNode[0]).length);
	}

	@Test
	public void testGrouping_AutoGroupConnections() {
		VisualModel model = createModel();

		VisualGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

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

		VisualGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

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

		VisualGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con1 = Tools.createConnection(c1, c2, root);
		VisualConnection con2 = Tools.createConnection(c1, c2, root);

		model.addToSelection(c1);
		model.addToSelection(c2);
		model.addToSelection(con1);
		model.addToSelection(con2);
		model.groupSelection();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {c1, c2, con1, con2}) },
				root.getChildren().toArray(new VisualNode[0]));
	}

	@Test
	public void testGrouping_AutoGroupConnectionsPointingDeep() {
		VisualModel model = createModel();

		VisualGroup root = model.getRoot();
		VisualGroup node1 = Tools.createGroup(root);
		VisualComponent c1 = Tools.createComponent(node1);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

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

		VisualGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualComponent c3 = Tools.createComponent(root);
		VisualConnection con1 = Tools.createConnection(c1, c2, root);
		VisualConnection con2 = Tools.createConnection(c2, c3, root);

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

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof VisualGroup))
				return false;
			VisualGroup group = (VisualGroup) obj;
			VisualNode[] my = expected;
			VisualNode[] their = group.getChildren().toArray(new VisualNode[0]);;
			if (my.length != their.length)
				return false;
			for (int i = 0; i < my.length; i++)
				if (!(my[i].equals(their[i])))
					return false;
			return true;
		}
	}

	@Test
	public void testGrouping_DontGroupConnections() {
		VisualModel model = createModel();

		VisualGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualComponent c3 = Tools.createComponent(root);
		VisualConnection con1 = Tools.createConnection(c1, c2, root);

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

		VisualGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

		model.addToSelection(c1);
		model.addToSelection(con);
		model.groupSelection();
		Assert.assertArrayEquals(new VisualNode[] { c1, c2, con }, root
				.getChildren().toArray(new VisualNode[0]));
	}

	private VisualModel createModel() {
		try {
			return new VisualModel(new MockMathModel());
		} catch (VisualModelInstantiationException e) {
			throw new RuntimeException("error!");
		}
	}

	@Test
	public void TestHitNodeCurrentLevelTransformation()
	{
		VisualModel model = createModel();

		VisualGroup root = model.getRoot();
		VisualGroup group1 = Tools.createGroup(root);
		group1.setX(101);
		SquareNode sq = new SquareNode(group1, new Rectangle2D.Double(0, 0, 1, 1));
		group1.add(sq);

		Assert.assertNull(model.hitNode(new Point2D.Double(0.5, 0.5)));
		Assert.assertEquals(group1, model.hitNode(new Point2D.Double(101.5, 0.5)));
		model.setCurrentLevel(group1);
		Assert.assertNull(model.hitNode(new Point2D.Double(0.5, 0.5)));
		Assert.assertEquals(sq, model.hitNode(new Point2D.Double(101.5, 0.5)));
	}

	@Test
	public void TestHitObjectsCurrentLevelTransformation()
	{
		VisualModel model = createModel();

		VisualGroup root = model.getRoot();
		VisualGroup group1 = Tools.createGroup(root);
		group1.setX(101);
		SquareNode sq = new SquareNode(group1, new Rectangle2D.Double(0, 0, 1, 1));
		group1.add(sq);

		SquareNode sq2 = new SquareNode(root, new Rectangle2D.Double(0, 5, 1, 1));
		root.add(sq2);

		Assert.assertEquals(0, model.hitObjects(new Rectangle2D.Double(-0.01, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(1, model.hitObjects(new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(group1, model.hitObjects(new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).get(0));
		Assert.assertEquals(1, model.hitObjects(new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).size());
		Assert.assertEquals(sq2, model.hitObjects(new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).get(0));
		model.setCurrentLevel(group1);
		Assert.assertEquals(0, model.hitObjects(new Rectangle2D.Double(-0.01, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(1, model.hitObjects(new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).size());
		Assert.assertEquals(sq, model.hitObjects(new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).get(0));
		Assert.assertEquals(0, model.hitObjects(new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).size());
	}
}
