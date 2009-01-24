package org.workcraft.testing.dom.visual;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualComponentGroup;
import org.workcraft.dom.visual.VisualConnection;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.VisualModelConstructionException;

public class VisualModelTests {

	private class MockMathModel extends MathModel {

		public MockMathModel() {
			super(null);
		}

		@Override
		public void validate() throws ModelValidationException {
			return;
		}

		@Override
		protected void validateConnection(Connection connection)
				throws InvalidConnectionException {
			return;
		}
	}

	@Test
	public void TestGroupWithEmptySelection()
			throws VisualModelConstructionException {
		VisualModel model = new VisualModel(new MockMathModel());

		model.getCurrentLevel().add(new VisualComponentGroup(null));

		VisualNode[] old = model.getCurrentLevel().getChildren();
		Assert.assertEquals(1, old.length);
		model.group();
		VisualNode[] _new = model.getCurrentLevel().getChildren();
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

		VisualNode[] old = model.getCurrentLevel().getChildren();

		model.group();

		VisualNode[] _new = model.getCurrentLevel().getChildren();

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

		VisualComponentGroup newGroup = (VisualComponentGroup) _new[_new.length - 1];
		Assert.assertFalse(oldList.contains(newGroup));
		Assert.assertTrue(newGroup instanceof VisualComponentGroup);

		ArrayList<VisualNode> newNodeList = new ArrayList<VisualNode>();
		for (VisualNode node : newGroup.getChildren())
			newNodeList.add(node);

		Assert.assertEquals(toGroup.length, newNodeList.size());
		for (VisualNode node : toGroup)
			Assert.assertTrue(newNodeList.contains(node));
	}

	@Test
	public void TestGroup2Items() throws VisualModelConstructionException {
		VisualModel model = new VisualModel(new MockMathModel());

		VisualComponentGroup root = model.getCurrentLevel();
		VisualComponentGroup node1 = Tools.createGroup(root);
		VisualComponentGroup node2 = Tools.createGroup(root);

		TestGroup(model, new VisualNode[] { node1, node2 });
	}

	@Test
	public void TestGroup1Item() throws VisualModelConstructionException {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getCurrentLevel();
		VisualComponentGroup node1 = Tools.createGroup(root);

		model.addToSelection(node1);
		model.group();
		Assert.assertEquals(1, root.getChildren().length);
		Assert.assertEquals(node1, root.getChildren()[0]);
		Assert.assertEquals(0, node1.getChildren().length);
	}

	@Test
	public void TestGroup5Items() throws VisualModelConstructionException {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getCurrentLevel();
		VisualComponentGroup node1 = new VisualComponentGroup(root);
		VisualComponentGroup node2 = new VisualComponentGroup(root);
		VisualComponentGroup node3 = new VisualComponentGroup(root);
		VisualComponentGroup node4 = new VisualComponentGroup(root);
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
	public void TestUngroupRoot() throws VisualModelConstructionException {
		VisualModel model = new VisualModel(new MockMathModel());

		VisualComponentGroup root = model.getCurrentLevel();

		VisualComponentGroup node1 = new VisualComponentGroup(root);
		VisualComponentGroup node2 = new VisualComponentGroup(node1);
		VisualComponentGroup node3 = new VisualComponentGroup(node2);
		VisualComponentGroup node4 = new VisualComponentGroup(node1);
		VisualComponentGroup node5 = new VisualComponentGroup(root);

		node2.add(node3);
		node1.add(node2);
		node1.add(node4);
		root.add(node1);
		root.add(node5);

		model.addToSelection(node1);
		model.ungroup();

		VisualNode[] newList = root.getChildren();

		Assert.assertEquals(3, newList.length);
		Assert.assertSame(node5, newList[0]);
		Assert.assertSame(node2, newList[1]);
		Assert.assertSame(node4, newList[2]);

		VisualNode[] n2Children = node2.getChildren();
		Assert.assertEquals(1, n2Children.length);
		Assert.assertSame(node3, n2Children[0]);
	}

	@Test
	public void TestUngroupNonRoot() throws VisualModelConstructionException {
		VisualModel model = new VisualModel(new MockMathModel());

		VisualComponentGroup root = model.getCurrentLevel();

		VisualComponentGroup node1 = new VisualComponentGroup(root);
		VisualComponentGroup node2 = new VisualComponentGroup(node1);
		VisualComponentGroup node3 = new VisualComponentGroup(node2);
		VisualComponentGroup node4 = new VisualComponentGroup(node1);
		VisualComponentGroup node5 = new VisualComponentGroup(root);

		node2.add(node3);
		node1.add(node2);
		node1.add(node4);
		root.add(node1);
		root.add(node5);

		model.setCurrentLevel(node1);

		model.addToSelection(node2);
		model.ungroup();

		VisualNode[] newList = root.getChildren();

		Assert.assertEquals(2, newList.length);
		Assert.assertSame(node1, newList[0]);
		Assert.assertSame(node5, newList[1]);

		VisualNode[] n1Children = node1.getChildren();
		Assert.assertEquals(2, n1Children.length);
		Assert.assertSame(node4, n1Children[0]);
		Assert.assertSame(node3, n1Children[1]);
	}

	@Test
	public void TestUngroupEmpty() throws VisualModelConstructionException {
		VisualModel model = new VisualModel(new MockMathModel());

		VisualComponentGroup root = model.getCurrentLevel();

		VisualComponentGroup node1 = new VisualComponentGroup(root);
		VisualComponentGroup node2 = new VisualComponentGroup(root);

		root.add(node1);
		root.add(node2);

		model.addToSelection(node2);
		model.ungroup();

		VisualNode[] newList = root.getChildren();

		Assert.assertEquals(1, newList.length);
		Assert.assertSame(node1, newList[0]);

		Assert.assertEquals(0, node1.getChildren().length);
	}

	@Test
	public void TestUngroupTwoGroups() throws VisualModelConstructionException {
		VisualModel model = new VisualModel(new MockMathModel());

		VisualComponentGroup root = model.getCurrentLevel();

		VisualComponentGroup node1 = new VisualComponentGroup(root);
		VisualComponentGroup node2 = new VisualComponentGroup(root);
		VisualComponentGroup node3 = new VisualComponentGroup(node1);
		VisualComponentGroup node4 = new VisualComponentGroup(node2);

		root.add(node1);
		root.add(node2);

		node1.add(node3);
		node2.add(node4);

		model.addToSelection(node2);
		model.addToSelection(node1);
		model.ungroup();

		VisualNode[] newList = root.getChildren();

		Assert.assertEquals(2, newList.length);
		Assert.assertSame(node4, newList[0]);
		Assert.assertSame(node3, newList[1]);

		Assert.assertEquals(0, node3.getChildren().length);
		Assert.assertEquals(0, node4.getChildren().length);
	}

	@Test
	public void testGrouping_AutoGroupConnections() {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

		model.addToSelection(c1);
		model.addToSelection(c2);
		model.group();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {c1, c2, con}) },
				root.getChildren());
	}

	@Test
	public void testGrouping_AutoGroupConnectionsIgnoreSelection() {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

		model.addToSelection(con);
		model.addToSelection(c1);
		model.addToSelection(c2);
		model.group();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {c1, c2, con}) },
				root.getChildren());
	}
	@Test
	public void testGrouping_AutoGroupTwoConnections() {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con1 = Tools.createConnection(c1, c2, root);
		VisualConnection con2 = Tools.createConnection(c1, c2, root);

		model.addToSelection(c1);
		model.addToSelection(c2);
		model.addToSelection(con1);
		model.addToSelection(con2);
		model.group();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {c1, c2, con1, con2}) },
				root.getChildren());
	}

	@Test
	public void testGrouping_AutoGroupConnectionsPointingDeep() {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getRoot();
		VisualComponentGroup node1 = Tools.createGroup(root);
		VisualComponent c1 = Tools.createComponent(node1);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

		model.addToSelection(node1);
		model.addToSelection(c2);
		model.group();
		Assert.assertArrayEquals(
				new Object[] { new GroupNodeEqualityTest(new VisualNode[] {node1, c2, con}) },
				root.getChildren());
	}

	@Test
	public void testGrouping_DontGroupConnectionsSimple() {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualComponent c3 = Tools.createComponent(root);
		VisualConnection con1 = Tools.createConnection(c1, c2, root);
		VisualConnection con2 = Tools.createConnection(c2, c3, root);

		model.addToSelection(con1);
		model.addToSelection(con2);
		model.group();

		Assert.assertArrayEquals(new VisualNode[] { c1, c2, c3, con1, con2 },
				root.getChildren());
	}

	class GroupNodeEqualityTest {
		private VisualNode[] expected;

		public GroupNodeEqualityTest(VisualNode[] expected) {
			this.expected = expected;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof VisualComponentGroup))
				return false;
			VisualComponentGroup group = (VisualComponentGroup) obj;
			VisualNode[] my = expected;
			VisualNode[] their = group.getChildren();
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

		VisualComponentGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualComponent c3 = Tools.createComponent(root);
		VisualConnection con1 = Tools.createConnection(c1, c2, root);

		model.addToSelection(con1);
		model.addToSelection(c2);
		model.addToSelection(c3);
		model.group();

		Assert.assertArrayEquals(new Object[] { c1, con1,
				new GroupNodeEqualityTest(new VisualNode[] { c2, c3 }) },
				root.getChildren());
	}

	@Test
	public void testGrouping_DontCountConnections() {
		VisualModel model = createModel();

		VisualComponentGroup root = model.getRoot();
		VisualComponent c1 = Tools.createComponent(root);
		VisualComponent c2 = Tools.createComponent(root);
		VisualConnection con = Tools.createConnection(c1, c2, root);

		model.addToSelection(c1);
		model.addToSelection(con);
		model.group();
		Assert.assertArrayEquals(new VisualNode[] { c1, c2, con }, root
				.getChildren());
	}

	private VisualModel createModel() {
		try {
			return new VisualModel(new MockMathModel());
		} catch (VisualModelConstructionException e) {
			throw new RuntimeException("error!");
		}
	}
}
