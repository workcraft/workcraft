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

package org.workcraft.plugins.balsa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class VisualHandshake extends VisualComponent {

	private final Handshake handshake;

	VisualHandshake(BreezeHandshake handshake)
	{
		super(handshake);
		this.handshake = handshake.getHandshake();
	}

	public BreezeHandshake getHandshakeComponent() {
		return (BreezeHandshake)getReferencedComponent();
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-0.5, -0.5, 1, 1);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return pointInLocalSpace.distanceSq(0, 0) < 0.25;
	}

	public Set<MathNode> getMathReferences() {
		return new HashSet<MathNode>();
	}

	public void draw(DrawRequest r) {
		r.setStroke(new BasicStroke(0.1f));
		Ellipse2D.Double circle = new Ellipse2D.Double(-0.5, -0.5, 1, 1);
		r.setColor(handshake.isActive()?Color.BLACK : Color.WHITE);
		r.fill(circle);
		r.setColor(Color.BLACK);
		r.draw(circle);


		/*if(handshake instanceof DataHandshake)
			drawDataConnector(g, (DataHandshake) handshake);
		else
			g.draw(new Line2D.Double(0.5, 0, 4, 0));*/
	}

	/*private void drawDataConnector(Graphics2D g, DataHandshake dataHandshake) {
		Stroke stroke = g.getStroke();

		g.setStroke(new BasicStroke(0.05f));

		double arrowPointX;
		double arrowBaseX;
		double widthSpecificationX;
		double lineStartX, lineEndX;

		if(dataHandshake.isActive() == dataHandshake instanceof PullHandshake)
		{
			arrowPointX = 0.5;
			arrowBaseX = 2;
			widthSpecificationX = 2.5;
			lineStartX = 1;
			lineEndX = 4;
		}
		else
		{
			arrowPointX = 4.0;
			arrowBaseX = 2.5;
			widthSpecificationX = 1.5;
			lineStartX = 0.5;
			lineEndX = 3.5;
		}

		g.draw(new Line2D.Double(widthSpecificationX, 0.5, widthSpecificationX+0.5, -0.5));

		Path2D.Double arrow = new Path2D.Double();
		arrow.moveTo(arrowPointX, 0);
		arrow.lineTo(arrowBaseX, -0.5);
		arrow.lineTo(arrowBaseX, 0.5);
		g.fill(arrow);

		g.setStroke(stroke);

		g.draw(new Line2D.Double(lineStartX, 0, lineEndX, 0));
	}*/

	public Handshake getHandshake() {
		return handshake;
	}

}
