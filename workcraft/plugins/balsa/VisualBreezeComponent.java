package org.workcraft.plugins.balsa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNodeDeserialiser;
import org.workcraft.framework.VisualNodeSerialiser;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.layouts.MainLayouter;
import org.workcraft.util.XmlUtil;

public class VisualBreezeComponent extends VisualGroup {

	HandshakeComponentLayout layout;
	Map<String, VisualHandshake> visualHandshakes;
	Map<Handshake,HandshakeComponent> handshakeComponents;
	Map<String,Handshake> handshakes;
	HandshakeVisualLayout visualLayout;
	Component balsaComponent;

	private static final double sideDoubleHandshakeAngle = 3.141592/4; // 45 degrees in radians
	private static final double componentRadius = 0.5;
	private static final double handshakeRadius = componentRadius/5;
	private final BreezeComponent refComponent;

	public VisualBreezeComponent(Element element, VisualModel model) throws VisualConnectionCreationException, VisualComponentCreationException {
		this(getRefComponent(element, model), element);
	}

	private static BreezeComponent getRefComponent(Element element,
			VisualModel model) {
		Element refElement = XmlUtil.getChildElement(VisualBreezeComponent.class.getSimpleName(), element);
		int id = XmlUtil.readIntAttr(refElement, "ref", -1);

		return (BreezeComponent)model.getMathModel().getComponentByRenamedID(id);
	}

	public VisualBreezeComponent(BreezeComponent refComponent, Element element) throws VisualConnectionCreationException, VisualComponentCreationException {
		VisualTransformableNodeDeserialiser.initTransformableNode(element, this);

		this.refComponent = refComponent;
		init();
	}

	private void init() {
		balsaComponent = refComponent.getUnderlyingComponent();
		handshakeComponents = refComponent.getHandshakeComponents();
		handshakes = refComponent.getHandshakes();
		layout = MainLayouter.getLayout(balsaComponent, handshakes);
		buildVisualHandshakes();
		makeProperties();
	}

	public VisualBreezeComponent(BreezeComponent refComponent) {
		this.refComponent = refComponent;
		init();
	}

	public VisualNodeSerialiser getSerialiser() {
		return new VisualBreezeSerialiser();
	}

	private void makeProperties() {
		try {
			BeanInfo info = Introspector.getBeanInfo(balsaComponent.getClass());
			PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
			for(int i=0;i<descriptors.length;i++)
				if(!descriptors[i].getName().equals("class"))
					addPropertyDeclaration(new BreezePropertyDescriptor(descriptors[i]));
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	private void buildVisualHandshakes() {

		visualLayout = new VisualLayouter().getVisualLayout(layout);
		visualHandshakes = new HashMap<String, VisualHandshake>();

		for(String name : handshakes.keySet())
		{
			Handshake handshake = handshakes.get(name);
			Ray ray = visualLayout.positions.get(handshake);
			VisualHandshake visual = new VisualHandshake(handshakeComponents.get(handshake));
			visualHandshakes.put(name, visual);
			Direction dir = ray.direction;
			int quadrants =
				dir == Direction.Right ? 0 :
					dir == Direction.Down ? 1:
						dir == Direction.Left ? 2:
							3;

			try {
				visual.applyTransform(AffineTransform.getQuadrantRotateInstance(quadrants));
				visual.applyTransform(AffineTransform.getScaleInstance(handshakeRadius*2, handshakeRadius*2));
				visual.applyTransform(AffineTransform.getTranslateInstance(ray.position.x, ray.position.y));
			} catch (NoninvertibleTransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.add(visual);
		}
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D result = new Rectangle2D.Double(-0.5, -0.5, 1, 1);

		Rectangle2D parentBB = super.getBoundingBoxInLocalSpace();
		if(parentBB != null)
			result.add(parentBB);

		return result;
	}

	@Override
	public Touchable hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if(pointInLocalSpace.distanceSq(0.0, 0.0) < 0.25)
			return this;

		return super.hitTestInLocalSpace(pointInLocalSpace);
	}

	enum Direction { Up, Right, Down, Left	};


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
		HashMap<Handshake, Ray> positions;
		SideLine left;
		SideLine right;
	}

	private void drawCircle(Graphics2D g, double x, double y, double r)
	{
		g.draw(new Ellipse2D.Double(x-r, y-r, r*2, r*2));
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		g.setStroke(new BasicStroke(0.02f));
		g.setColor(Coloriser.colorise(Color.black, this.getColorisation()));

		drawSideLine(g, visualLayout.left, -1);
		drawSideLine(g, visualLayout.right, +1);

		drawCircle(g, 0, 0, componentRadius);

		Font font = g.getFont();
		font = font.deriveFont(0.1f);
		GlyphVector vec = font.createGlyphVector(g.getFontRenderContext(), balsaComponent.getClass().getSimpleName());
		Rectangle2D bounds = vec.getVisualBounds();
		g.drawGlyphVector(vec, (float)-bounds.getCenterX(), (float)-bounds.getCenterY());

		super.drawInLocalSpace(g);
	}

	private void drawSideLine(Graphics2D g, SideLine extent, int dir) {
		double x = componentRadius*dir;
		g.draw(new Line2D.Double(x, extent.top, x, extent.bottom));
	}

	private static class VisualLayouter
	{
		HashMap<Handshake, Ray> positions;

		public HandshakeVisualLayout getVisualLayout(HandshakeComponentLayout layout) {
			HandshakeVisualLayout result = new HandshakeVisualLayout();

			result.positions = positions = new HashMap<Handshake, Ray>();
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
			positions.put(handshake, new Ray(0, (componentRadius+handshakeRadius)*direction, dir));
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

					positions.put(handshakes[i][j], new Ray(x, current, dir));
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

			positions.put(handshake1, new Ray(x, -y, dir));
			positions.put(handshake2, new Ray(x, y, dir));
		}

		private void layoutSideSingleHandshake(Handshake handshake, int direction) {
			positions.put(handshake,
					new Ray(
							direction*(componentRadius+handshakeRadius), 0,
							direction<0 ? Direction.Left : Direction.Right));
		}
	}

	public BreezeComponent getRefComponent() {
		return refComponent;
	}
}
