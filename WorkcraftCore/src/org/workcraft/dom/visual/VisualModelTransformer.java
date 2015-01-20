package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.util.Hierarchy;

public class VisualModelTransformer {
	/**
	 * Only transforms node position (not orientation)
	 * @author Stanislav Golubtsov
	 *
	 **/
	public static void transformNodePosition(Collection<Node> nodes, AffineTransform t) {
		assert nodes!=null;
		for (Node node: nodes) {
			// do transformation group children
			if ((node instanceof VisualGroup) || (node instanceof VisualPage)) {
				VisualTransformableNode vt  = (VisualTransformableNode) node;
				AffineTransform t2 = new AffineTransform();
				t2.translate(-vt.getX(), -vt.getY());
				t2.concatenate(t);
				t2.translate(vt.getX(), vt.getY());
				transformNodePosition(vt.getChildren(), t2);
			} else if (node instanceof VisualTransformableNode) {
				VisualTransformableNode vn = (VisualTransformableNode) node;
				Point2D np = vn.getPosition();
				t.transform(np, np);
				vn.setPosition(np);
			} else if (node instanceof Movable) {
				Movable mv = (Movable) node;
				MovableHelper.translate(mv, t.getTranslateX(), t.getTranslateY());
			}
		}
	}

	public static void translateNodes(Collection<Node> nodes, double tx, double ty) {
		AffineTransform t = AffineTransform.getTranslateInstance(tx, ty);
		transformNodePosition(nodes, t);
	}

	public static void translateSelection(VisualModel vm, double tx, double ty) {
		translateNodes(vm.getSelection(), tx, ty);
	}

	public static void scaleSelection(VisualModel vm, double sx, double sy) {
		Rectangle2D selectionBB = getNodesCoordinateBox(vm.getSelection());
		if (selectionBB != null) {
			AffineTransform t = new AffineTransform();

			t.translate(selectionBB.getCenterX(), selectionBB.getCenterY());
			t.scale(sx, sy);
			t.translate(-selectionBB.getCenterX(), -selectionBB.getCenterY());

			transformNodePosition(vm.getSelection(), t);
		}
	}

	public static void rotateSelection(VisualModel vm, double theta) {
		Rectangle2D selectionBB = getNodesCoordinateBox(vm.getSelection());
		if (selectionBB != null) {
			AffineTransform t = new AffineTransform();
			Point2D cp = (new Point2D.Double(selectionBB.getCenterX(), selectionBB.getCenterY()));

			t.translate(cp.getX(), cp.getY());
			t.rotate(theta);
			t.translate(-cp.getX(), -cp.getY());

			transformNodePosition(vm.getSelection(), t);
		}
	}

	private static Rectangle2D bbUnion(Rectangle2D bb1, Point2D bb2) {
		if (bb2 == null) {
			return bb1;
		}

		Rectangle2D r = new Rectangle2D.Double(bb2.getX(), bb2.getY(), 0, 0);
		if (bb1 == null) {
			bb1 = r;
		} else Rectangle2D.union(bb1, r, bb1);

		return bb1;
	}

	public static Rectangle2D getNodesCoordinateBox(Collection<Node> nodes) {
		Rectangle2D selectionBB = null;
		for (Node vn: nodes) {
			if (vn instanceof VisualGroup) {
				Rectangle2D r = getNodesCoordinateBox(((VisualGroup)vn).getChildren());
				Point2D p = ((VisualGroup)vn).getPosition();
				r.setRect(r.getX()+p.getX(), r.getY()+p.getY(), r.getWidth(), r.getHeight());

				if (selectionBB==null)
					selectionBB = r;
				else if (r!=null)
					Rectangle2D.union(selectionBB, r, selectionBB);
			} else if(vn instanceof VisualTransformableNode)
				selectionBB = bbUnion(selectionBB, ((VisualTransformableNode)vn).getPosition());
		}
		return selectionBB;
	}

	// FIXME: A hack to preserve the shape of connections on relocation of their adjacent components.
	public static void translateSelectionAndControlPoints(VisualModel vm, double tx, double ty) {
		Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(vm.getRoot(), VisualConnection.class);
		HashMap<VisualConnection, ScaleMode> connectionToScaleModeMap =	setConnectionsScaleMode(connections, ScaleMode.LOCK_RELATIVELY);
		translateNodes(vm.getSelection(), tx, ty);
		setConnectionsScaleMode(connectionToScaleModeMap);
	}

	public static HashMap<VisualConnection, ScaleMode> setConnectionsScaleMode(Collection<VisualConnection> connections, ScaleMode scaleMode) {
		HashMap<VisualConnection, ScaleMode> connectionToScaleModeMap = new HashMap<>();
		for (VisualConnection vc: connections) {
			connectionToScaleModeMap.put(vc, vc.getScaleMode());
			vc.setScaleMode(scaleMode);
		}
		return connectionToScaleModeMap;
	}

	public static void setConnectionsScaleMode(HashMap<VisualConnection, ScaleMode> connectionToScaleModeMap) {
		if (connectionToScaleModeMap != null) {
			for (Entry<VisualConnection, ScaleMode> entry: connectionToScaleModeMap.entrySet()) {
				VisualConnection vc = entry.getKey();
				ScaleMode scaleMode = entry.getValue();
				vc.setScaleMode(scaleMode);
			}
		}
	}

	public static HashMap<VisualTransformableNode, Point2D> getRootSpacePositions(Collection<Node> nodes) {
		HashMap<VisualTransformableNode, Point2D> componentToPositionMap = new HashMap<>();
		for (Node node: nodes) {
			if (node instanceof VisualTransformableNode) {
				VisualTransformableNode component = (VisualTransformableNode)node;
				Point2D position = component.getRootSpacePosition();
				componentToPositionMap.put(component, position);
			}
		}
		return componentToPositionMap;
	}

	public static void setRootSpacePositions(HashMap<VisualTransformableNode, Point2D> componentToPositionMap) {
		if (componentToPositionMap != null) {
			for (Entry<VisualTransformableNode, Point2D> entry: componentToPositionMap.entrySet()) {
				VisualTransformableNode component = entry.getKey();
				Point2D position = entry.getValue();
				component.setRootSpacePosition(position);
			}
		}
	}

}
