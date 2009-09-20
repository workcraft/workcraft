/**
 *
 */
package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.Coloriser;

class PolylineAnchorPoint extends VisualConnectionAnchorPoint {
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


		public PolylineAnchorPoint(Polyline polyline) {
			this.polyline = polyline;
		}

		public Rectangle2D getBoundingBoxInLocalSpace() {
			return new Rectangle2D.Double(-size/2, -size/2, size, size);
		}

		public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
			if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
				return true;
			else
				return false;
		}

		@Override
		public void draw(Graphics2D g) {
			g.setColor(Coloriser.colorise(fillColor, getColorisation()));
			g.fill(shape);
		}

		public int getIndex() {
			return index;
		}
	}