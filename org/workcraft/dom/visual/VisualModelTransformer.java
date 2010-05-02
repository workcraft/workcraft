package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.Node;

public class VisualModelTransformer {
	/**
	 * @author Stanislav Golubtsov
	 *   Only transforms node position (not orientation)
	 **/
	public static void transformNodePosition(Collection<Node> nodes, AffineTransform t) {
		assert nodes!=null;
		for (Node node: nodes) {
			if (node instanceof VisualTransformableNode) {
				VisualTransformableNode vn = (VisualTransformableNode) node;

				Point2D np=vn.getPosition();
				t.transform(np, np);
				vn.setPosition(np);
			}
		}
//			TransformHelper.applyTransform(node, t);
	}
	public static void translateNodes(Collection<Node> nodes, double tx, double ty) {
		AffineTransform t = AffineTransform.getTranslateInstance(tx, ty);

		transformNodePosition(nodes, t);
	}

	public static void translateSelection(VisualModel vm, double tx, double ty) {
		translateNodes(vm.getSelection(), tx, ty);
	}

	public static void scaleSelection(VisualModel vm, double sx, double sy) {
		Rectangle2D selectionBB = getNodesBoundingBox(vm.getSelection());

		AffineTransform t = new AffineTransform();

		t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
		t.scale(sx, sy);
		t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		transformNodePosition(vm.getSelection(), t);
	}

	public static void rotateSelection(VisualModel vm, double theta) {
		Rectangle2D selectionBB = getNodesBoundingBox(vm.getSelection());

		AffineTransform t = new AffineTransform();

		t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
		t.rotate(theta);
		t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

		transformNodePosition(vm.getSelection(), t);
	}

	private static Rectangle2D bbUnion(Rectangle2D bb1, Rectangle2D bb2)
	{
		if(bb1 == null)
			return bb2;
		if(bb2 == null)
			return bb1;
		Rectangle2D.union(bb1, bb2, bb1);
		return bb1;
	}

	public static Rectangle2D getNodesBoundingBox(Collection<Node> nodes) {
		Rectangle2D selectionBB = null;

		if (nodes.isEmpty()) return selectionBB;

		for (Node vn: nodes) {
			if(vn instanceof Touchable)
				selectionBB = bbUnion(selectionBB, ((Touchable)vn).getBoundingBox());
		}
		return selectionBB;
	}

}
