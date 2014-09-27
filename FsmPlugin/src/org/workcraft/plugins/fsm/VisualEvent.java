package org.workcraft.plugins.fsm;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Geometry;

public class VisualEvent extends VisualConnection {
	public static final Font symbolFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

	private RenderedText symbolRenderedText = new RenderedText("", symbolFont, Positioning.CENTER, new Point2D.Double());
	private Color symbolColor = CommonVisualSettings.getLabelColor();

	public VisualEvent() {
		this(null, null, null);
	}

	public VisualEvent(Event mathConnection) {
		this(mathConnection, null, null);
	}

	public VisualEvent(Event mathConnection, VisualState first, VisualState second) {
		super(mathConnection, first, second);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualEvent, String>(
				this, "Symbol", String.class) {
			public void setter(VisualEvent object, String value) {
				object.getReferencedEvent().setSymbol(value);
			}
			public String getter(VisualEvent object) {
				return object.getReferencedEvent().getSymbol();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualEvent, Color>(
				this, "Symbol color", Color.class) {
			protected void setter(VisualEvent object, Color value) {
				object.setSymbolColor(value);
			}
			protected Color getter(VisualEvent object) {
				return object.getSymbolColor();
			}
		});

	}

	public Event getReferencedEvent() {
		return (Event)getReferencedConnection();
	}

	public boolean getSymbolVisibility() {
		return CommonVisualSettings.getNameVisibility();
	}

	protected void cacheSymbolRenderedText(DrawRequest r) {
		String symbol = getReferencedEvent().getSymbol();
		if (symbol == null || symbol.equals("")) {
			symbol = "ε";
		}
		if (symbolRenderedText.isDifferent(symbol, symbolFont, Positioning.CENTER, new Point2D.Double())) {
			symbolRenderedText = new RenderedText(symbol, symbolFont, Positioning.CENTER, new Point2D.Double());
		}
	}

	private AffineTransform getSymbolTransform() {
		ConnectionGraphic graphic = getGraphic();
		Point2D middlePoint = graphic.getPointOnCurve(0.5);
		Point2D firstDerivative = graphic.getDerivativeAt(0.5);
		Point2D secondDerivative = graphic.getSecondDerivativeAt(0.5);
		if (firstDerivative.getX() < 0) {
			firstDerivative = Geometry.multiply(firstDerivative, -1);
		}

		Rectangle2D bb = symbolRenderedText.getBoundingBox();
		Point2D symbolPosition = new Point2D.Double(bb.getCenterX(), bb.getMaxY());
		if (Geometry.crossProduct(firstDerivative, secondDerivative) < 0) {
			symbolPosition.setLocation(symbolPosition.getX(), bb.getMinY());
		}

		AffineTransform transform = AffineTransform.getTranslateInstance(
				middlePoint.getX() - symbolPosition.getX(), middlePoint.getY() - symbolPosition.getY());
		AffineTransform rotateTransform = AffineTransform.getRotateInstance(
				firstDerivative.getX(), firstDerivative.getY(), symbolPosition.getX(), symbolPosition.getY());
		transform.concatenate(rotateTransform);
		return transform;
	}

	protected void drawSymbolInLocalSpace(DrawRequest r) {
		if (getSymbolVisibility()) {
			cacheSymbolRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();

			AffineTransform oldTransform = g.getTransform();
			AffineTransform transform = getSymbolTransform();
			g.transform(transform);
			g.setColor(Coloriser.colorise(symbolColor, d.getColorisation()));
			symbolRenderedText.draw(g);
			g.setTransform(oldTransform);
		}
	}

	@Override
	public void draw(DrawRequest r) {
		drawSymbolInLocalSpace(r);
	}

	private Rectangle2D getSymbolBoundingBox() {
		return BoundingBoxHelper.transform(symbolRenderedText.getBoundingBox(), getSymbolTransform());
	}

	@Override
	public Rectangle2D getBoundingBox() {
		Rectangle2D symbolBB = getSymbolBoundingBox();
		return BoundingBoxHelper.union(super.getBoundingBox(), symbolBB);
	}


	@Override
	public boolean hitTest(Point2D pointInParentSpace) {
		Rectangle2D symbolBB = getSymbolBoundingBox();
		if (symbolBB != null && symbolBB.contains(pointInParentSpace)) return true;
		return super.hitTest(pointInParentSpace);
	}

	public Color getSymbolColor() {
		return symbolColor;
	}

	public void setSymbolColor(Color symbolColor) {
		this.symbolColor = symbolColor;
	}

}
