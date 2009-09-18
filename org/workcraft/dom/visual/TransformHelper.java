package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.NotAnAncestorException;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class TransformHelper {

	public static void applyTransform(Node node, AffineTransform transform) {
		if(node instanceof Movable)
			((Movable) node).applyTransform(transform);
		else
			applyTransformToChildNodes(node, transform);
	}

	public static void applyTransformToChildNodes(Node node, AffineTransform transform) {
		for(Node n: node.getChildren())
			applyTransform(n, transform);
	}

	public static AffineTransform getTransformToAncestor(Node node, Node ancestor) {
		AffineTransform t = new AffineTransform();

		while (ancestor != node) {
			Node next = node.getParent();
			if (next == null)
				throw new NotAnAncestorException();
			if(next instanceof Movable)
				t.preConcatenate(((Movable)next).getTransform());
			node = next;
		}

		return t;
	}

	public static AffineTransform getTransform(Node node1, Node node2) {
		Node parent = Hierarchy.getCommonParent(node1, node2);
		AffineTransform node1ToParent = getTransformToAncestor(node1, parent);
		AffineTransform node2ToParent = getTransformToAncestor(node2, parent);
		AffineTransform parentToNode2 = Geometry.optimisticInverse(node2ToParent);

		parentToNode2.concatenate(node1ToParent);
		return parentToNode2;
	}

	public static Touchable transform(Touchable touchable, AffineTransform transform)
	{
		return new TouchableTransformer(touchable, transform);
	}
}
