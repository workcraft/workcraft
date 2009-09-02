package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

import org.workcraft.dom.HierarchyNode;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class TransformHelper {

	public static void applyTransform(HierarchyNode node, AffineTransform transform) {
		if(node instanceof Movable)
			((Movable) node).applyTransform(transform);
		else
			applyTransformToChildNodes(node, transform);
	}

	public static void applyTransformToChildNodes(HierarchyNode node, AffineTransform transform) {
		for(HierarchyNode n: node.getChildren())
			applyTransform(n, transform);
	}

	public static AffineTransform getTransformToAncestor(HierarchyNode node, HierarchyNode ancestor) {
		AffineTransform t = new AffineTransform();

		while (ancestor != node) {
			HierarchyNode next = node.getParent();
			if (next == null)
				throw new NotAnAncestorException();
			if(next instanceof Movable)
				t.preConcatenate(((Movable)next).getTransform());
			node = next;
		}

		return t;
	}

	public static AffineTransform getTransform(HierarchyNode node1, HierarchyNode node2) {
		HierarchyNode parent = Hierarchy.getCommonParent(node1, node2);
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
