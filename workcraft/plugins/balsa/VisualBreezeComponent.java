package org.workcraft.plugins.balsa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;
import org.workcraft.plugins.balsa.layouts.MainLayouter;

public class VisualBreezeComponent extends VisualComponent {

	HandshakeComponentLayout layout;

	private static final double sideDoubleHandshakeAngle = 3.141592/4; // 45 degrees in radians
	private static final double componentRadius = 1;
	private static final double handshakeRadius = componentRadius/5;

	public VisualBreezeComponent(BreezeComponent refComponent) {
		super(refComponent);
		Component balsaComponent = refComponent.getUnderlyingComponent();
		layout = MainLayouter.getLayout(balsaComponent, MainHandshakeMaker.getHandshakes(balsaComponent));
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		// TODO Auto-generated method stub
		return 0;
	}

	enum Direction { Up, Right, Down, Left	};

	static class HandshakePosition
	{
		public HandshakePosition(double x, double y, Direction direction)
		{
			this.position = new Point2D.Double(x, y);
			this.direction = direction;
		}
		public final Point2D.Double position;
		public final Direction direction;
	}

	static class SideLine
	{
		public SideLine(double top, double bottom) {
			this.top = top;
			this.bottom = bottom;
		}
		double top;
		double bottom;
	}

	static class HandshakeVisualLayout
	{
		HashMap<Handshake, HandshakePosition> positions;
		SideLine left;
		SideLine right;
	}

	private void drawCircle(Graphics2D g, Point2D.Double center, double r)
	{
		drawCircle(g, center.x, center.y, r);
	}
	private void drawCircle(Graphics2D g, double x, double y, double r)
	{
		g.draw(new Ellipse2D.Double(x-r, y-r, r*2, r*2));
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		g.setStroke(new BasicStroke(0.05f));
		g.setColor(Color.black);

		HandshakeVisualLayout visualLayout = new VisualLayouter().getVisualLayout(layout);

		HashMap<Handshake, HandshakePosition> positions = visualLayout.positions;
		for (Handshake handshake : positions.keySet()) {
			HandshakePosition position = positions.get(handshake);
			drawCircle(g, position.position, handshakeRadius);
		}

		drawSideLine(g, visualLayout.left, -1);
		drawSideLine(g, visualLayout.right, +1);

		drawCircle(g, 0, 0, componentRadius);
	}

	private void drawSideLine(Graphics2D g, SideLine extent, int dir) {
		double x = componentRadius*dir;
		g.draw(new Line2D.Double(x, extent.top, x, extent.bottom));
	}

	private static class VisualLayouter
	{
		HashMap<Handshake, HandshakePosition> positions;

		public HandshakeVisualLayout getVisualLayout(HandshakeComponentLayout layout) {
			HandshakeVisualLayout result = new HandshakeVisualLayout();

			result.positions = positions = new HashMap<Handshake, HandshakePosition>();
			result.left = layoutSide(layout.getLeft(), -1);
			result.right = layoutSide(layout.getRight(), +1);
			layoutPole(layout.getTop(), -1);
			layoutPole(layout.getBottom(), +1);

			return result;
		}

		private void layoutPole(Handshake handshake, int direction) {
			if(handshake == null)
				return;
			Direction dir = direction > 0 ? Direction.Down : Direction.Up;
			positions.put(handshake, new HandshakePosition(0, (componentRadius+handshakeRadius)*direction, dir));
		}

		private SideLine layoutSide(Handshake[][] handshakes, int direction) {
			if(handshakes.length == 0)
				;
			else if(handshakes.length == 1 && handshakes[0].length == 1)
				layoutSideSingleHandshake(handshakes[0][0], direction);
			else if(handshakes.length == 1 && handshakes[0].length == 2)
				layoutSideDoubleHandshake(handshakes[0][0], handshakes[0][1], direction);
			else if(handshakes.length == 2 && handshakes[0].length == 1 && handshakes[1].length == 1)
				layoutSideDoubleHandshake(handshakes[0][0], handshakes[1][0], direction);
			else
				return layoutSideMultiHandshake(handshakes, direction);
			return new SideLine(0, 0);
		}

		private static final double stepBetweenHandshakes = handshakeRadius*3;
		private static final double stepBetweenGroups = handshakeRadius*5;

		double getHeight(Handshake[] handshakes)
		{
			return Math.max(0, (handshakes.length - 1) * stepBetweenHandshakes);
		}

		private SideLine layoutSideMultiHandshake(Handshake[][] handshakes, int direction) {

			double totalHeight = 0;
			double top;

			double x = (handshakeRadius + componentRadius) * direction;
			Direction dir = direction<0 ? Direction.Left : Direction.Right;

			for(int i=0;i<handshakes.length;i++)
			{
				if(i!=0)
					totalHeight += stepBetweenGroups;
				totalHeight += getHeight(handshakes[i]);
			}

			if(handshakes.length == 2)
				top = -getHeight(handshakes[0]) - stepBetweenGroups/2;
			else
				top = -totalHeight/2;

			double current = top;
			for(int i=0;i<handshakes.length;i++)
			{
				if(i!=0)
					current += stepBetweenGroups;
				for(int j=0;j<handshakes[i].length;j++)
				{
					if(j!=0)
						current += stepBetweenHandshakes;

					positions.put(handshakes[i][j], new HandshakePosition(x, current, dir));
				}
			}

			return new SideLine(top, top+totalHeight);
		}

		private void layoutSideDoubleHandshake(Handshake handshake1,
				Handshake handshake2, int direction) {

			new Point2D.Double(direction*(componentRadius+handshakeRadius), 0);
			double y = Math.sin(sideDoubleHandshakeAngle);
			double x = Math.sin(sideDoubleHandshakeAngle);
			x *= (componentRadius+handshakeRadius);
			y *= (componentRadius+handshakeRadius);

			x *= direction;

			Direction dir = direction<0 ? Direction.Left : Direction.Right;

			positions.put(handshake1, new HandshakePosition(x, -y, dir));
			positions.put(handshake2, new HandshakePosition(x, y, dir));
		}

		private void layoutSideSingleHandshake(Handshake handshake, int direction) {
			positions.put(handshake,
					new HandshakePosition(
							direction*(componentRadius+handshakeRadius), 0,
							direction<0 ? Direction.Left : Direction.Right));
		}
	}
}
