package org.workcraft.plugins.balsa;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.balsa.handshakebuilder.ActivePull;
import org.workcraft.plugins.balsa.handshakebuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakebuilder.DataHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePush;

public class VisualHandshake extends VisualComponent {

	private final Handshake handshake;

	VisualHandshake(HandshakeComponent handshake)
	{
		super(handshake);
		this.handshake = handshake.getHandshake();
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

	public void draw(Graphics2D g) {
		g.setStroke(new BasicStroke(0.1f));
		Ellipse2D.Double circle = new Ellipse2D.Double(-0.5, -0.5, 1, 1);
		g.draw(circle);
		if(handshake instanceof ActiveSync)
			g.fill(circle);


		if(handshake instanceof DataHandshake)
			drawDataConnector(g, (DataHandshake) handshake);
		else
			g.draw(new Line2D.Double(0.5, 0, 4, 0));
	}

	private void drawDataConnector(Graphics2D g, DataHandshake dataHandshake) {
		Stroke stroke = g.getStroke();

		g.setStroke(new BasicStroke(0.05f));

		double arrowPointX;
		double arrowBaseX;
		double widthSpecificationX;
		double lineStartX, lineEndX;

		if(dataHandshake instanceof ActivePull || dataHandshake instanceof PassivePush)
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
	}

	public Handshake getHandshake() {
		return handshake;
	}

}
