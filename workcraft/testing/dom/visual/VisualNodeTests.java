package org.workcraft.testing.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.exceptions.NotAnAncestorException;

public class VisualNodeTests {

	static VisualGroup createGroup(VisualGroup parent)
	{
		return Tools.createGroup(parent);
	}

	@Test
	public void testTransformChangeNotify()
	{
		final SquareNode node = new SquareNode(null, new Rectangle2D.Double(0, 0, 1, 1));
		final Boolean[] hit = new Boolean[]{false};
		node.addListener(new PropertyChangeListener()
				{
					@Override
					public void onPropertyChanged(String propertyName, Object sender) {
						if(propertyName=="transform" && node == sender)
							hit[0] = true;
					}
				});
		Assert.assertFalse("already hit o_O", hit[0]);
		node.setX(8);
		Assert.assertTrue("not hit", hit[0]);
	}

	@Test(timeout=1000)
	public void testParentToAncestorTransform() throws NotAnAncestorException
	{
		VisualGroup root = createGroup(null);
		VisualGroup node1 = createGroup(root);
		VisualGroup node2 = createGroup(root);
		VisualGroup node11 = createGroup(node1);
		VisualGroup node111 = createGroup(node11);
		VisualGroup node1111 = createGroup(node111);

		node1.setX(1);
		node1.setX(1);
		node11.setX(10);
		node111.setX(100);
		node1111.setX(1000);
		node2.setX(5);

		ensureShiftX(node1111, root, 111);
		ensureShiftX(node111, root, 11);
		ensureShiftX(node11, root, 1);
		ensureShiftX(node1, root, 0);

		ensureShiftX(node1111, node1, 110);
		ensureShiftX(node111, node1, 10);
		ensureShiftX(node11, node1, 0);
		ensureFall(node1, node1);

		ensureShiftX(node1111, node11, 100);
		ensureShiftX(node111, node11, 00);
		ensureFall(node11, node11);
		ensureFall(node1, node11);

		ensureShiftX(node1111, node111, 000);
		ensureFall(node111, node111);
		ensureFall(node11, node111);
		ensureFall(node1, node111);

		ensureFall(node1111, node1111);
		ensureFall(node111, node1111);
		ensureFall(node11, node1111);
		ensureFall(node1, node1111);

		ensureShiftX(node2, root, 0);
		ensureFall(node2, node1);
		ensureFall(node2, node11);
		ensureFall(node2, node111);
		ensureFall(node2, node1111);

		ensureFall(node1, node2);
		ensureFall(node11, node2);
		ensureFall(node111, node2);
		ensureFall(node1111, node2);
	}

	@Test
	public void TestGetPath()
	{
		VisualGroup root = createGroup(null);
		Assert.assertEquals(0, root.getPath().length);
		VisualGroup node1 = createGroup(root);
		Assert.assertArrayEquals(new VisualGroup[]{root}, node1.getPath());
		VisualGroup node2 = createGroup(node1);
		Assert.assertArrayEquals(new VisualGroup[]{root, node1}, node2.getPath());
	}

	@Test
	public void TestFindCommonParent()
	{
		VisualGroup root = createGroup(null);
		VisualGroup node1 = createGroup(root);
		VisualGroup node2 = createGroup(root);
		VisualGroup node11 = createGroup(node1);
		VisualGroup node12 = createGroup(node1);
		VisualGroup node21 = createGroup(node2);
		VisualGroup node22 = createGroup(node2);

		Assert.assertEquals(root, VisualNode.getCommonParent(node1, node2));
		Assert.assertEquals(root, VisualNode.getCommonParent(node1, node21));
		Assert.assertEquals(root, VisualNode.getCommonParent(node1, node22));

		Assert.assertEquals(root, VisualNode.getCommonParent(node11, node2));
		Assert.assertEquals(root, VisualNode.getCommonParent(node11, node21));
		Assert.assertEquals(root, VisualNode.getCommonParent(node11, node22));

		Assert.assertEquals(root, VisualNode.getCommonParent(node12, node2));
		Assert.assertEquals(root, VisualNode.getCommonParent(node12, node21));
		Assert.assertEquals(root, VisualNode.getCommonParent(node12, node22));

		Assert.assertEquals(root, VisualNode.getCommonParent(node11, node1));

		Assert.assertEquals(node1, VisualNode.getCommonParent(node11, node12));
		Assert.assertEquals(node1, VisualNode.getCommonParent(node11, node11));

		Assert.assertEquals(node1, VisualNode.getCommonParent(node12, node11));
		Assert.assertEquals(node1, VisualNode.getCommonParent(node12, node12));
	}

	private void ensureShiftX(VisualGroup node,
			VisualGroup ancestor, double i) throws NotAnAncestorException {
		ensureShiftX(node.getParentToAncestorTransform(ancestor), i);
		ensureShiftX(node.getAncestorToParentTransform(ancestor), -i);

	}

	private void ensureShiftX(AffineTransform transform, double i) {

		double [] matrix = new double[6];
		transform.getMatrix(matrix);
		asserArrayEquals(new double[]{1, 0, 0, 1, i, 0}, matrix);
	}

	private void asserArrayEquals(double[] expected, double[] actual) {
		Assert.assertEquals(expected.length, actual.length);
		for(int i=0;i<expected.length;i++)
			assertClose(expected[i], actual[i]);
	}

	private void assertClose(double expected, double actual)
	{
		double eps = 1e-6;
		Assert.assertTrue("Expected: "+expected+", actual: "+actual, expected-eps<=actual && expected+eps>=actual);
	}

	private void ensureFall(VisualGroup node, VisualGroup ancestor) {
		boolean ok1 = false;
		boolean ok2 = false;
		try
		{
			node.getAncestorToParentTransform(ancestor);
			ok1 = true;
		}
		catch(NotAnAncestorException ex)
		{
		}
		try
		{
			node.getParentToAncestorTransform(ancestor);
			ok2 = true;
		}
		catch(NotAnAncestorException ex)
		{
		}
		Assert.assertFalse(ok1);
		Assert.assertFalse(ok2);
	}
}
