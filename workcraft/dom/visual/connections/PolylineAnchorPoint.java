/**
 *
 */
package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualModelEventDispatcher;
import org.workcraft.gui.Coloriser;

class PolylineAnchorPoint extends VisualConnectionAnchorPoint {

		/**
		 *
		 */
		private final Polyline polyline;
		private int index;
		private double size = 0.25;
		private Color fillColor = Color.BLUE.darker();

		Shape shape = new Rectangle2D.Double(
				-size / 2,
				-size / 2,
				size,
				size);

		public void removeAnchorPoint(VisualConnectionAnchorPoint anchor) {
			this.polyline.anchorPoints.remove(anchor);
		}


		public PolylineAnchorPoint(Polyline polyline, VisualConnection parent) {
			super(parent);
			this.polyline = polyline;

		}

//		public PolylineAnchorPoint(int index) {
//			this.index = index;
//		}

		public Rectangle2D getBoundingBoxInLocalSpace() {
			return new Rectangle2D.Double(-size/2, -size/2, size, size);
		}

		public Touchable hitTestInLocalSpace(Point2D pointInLocalSpace) {
			if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
				return this;
			else
				return null;
		}

		@Override
		protected void drawInLocalSpace(Graphics2D g) {
			g.setColor(Coloriser.colorise(fillColor, getColorisation()));
			g.fill(shape);
		}

		public int getIndex() {
			return index;
		}


		public void subscribeEvents(VisualModelEventDispatcher eventDispatcher) {
		}


		public void unsubscribeEvents(VisualModelEventDispatcher eventDispatcher) {

		}
	}