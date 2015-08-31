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
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Geometry;

public class VisualEvent extends VisualConnection {
	public static final String PROPERTY_LABEL_COLOR = "Label color";

	// Epsilon symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
	public static final char EPSILON_SYMBOL = 0x03B5;

	public static final Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);

	private RenderedText labelRenderedText = new RenderedText("", labelFont, Positioning.CENTER, new Point2D.Double());
	private Color labelColor = CommonVisualSettings.getLabelColor();

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
		addPropertyDeclaration(new PropertyDeclaration<VisualEvent, Color>(
				this, PROPERTY_LABEL_COLOR, Color.class, true, true, true) {
			protected void setter(VisualEvent object, Color value) {
				object.setLabelColor(value);
			}
			protected Color getter(VisualEvent object) {
				return object.getLabelColor();
			}
		});

	}

	public Event getReferencedEvent() {
		return (Event)getReferencedConnection();
	}

	public boolean getLabelVisibility() {
		return CommonVisualSettings.getNameVisibility();
	}

	protected void cacheLabelRenderedText(DrawRequest r) {
		String labelText = getLabel(r);
		if (labelRenderedText.isDifferent(labelText, labelFont, Positioning.CENTER, new Point2D.Double())) {
			labelRenderedText = new RenderedText(labelText, labelFont, Positioning.CENTER, new Point2D.Double());
		}
	}

	public String getLabel(DrawRequest r) {
		String label = Character.toString(EPSILON_SYMBOL);
		Symbol symbol = getReferencedEvent().getSymbol();
		if (symbol != null) {
			label = r.getModel().getMathName(symbol);
		}
		return label;
	}

	private AffineTransform getLabelTransform() {
		ConnectionGraphic graphic = getGraphic();
		Point2D middlePoint = graphic.getPointOnCurve(0.5);
		Point2D firstDerivative = graphic.getDerivativeAt(0.5);
		Point2D secondDerivative = graphic.getSecondDerivativeAt(0.5);
		if (firstDerivative.getX() < 0) {
			firstDerivative = Geometry.multiply(firstDerivative, -1);
		}

		Rectangle2D bb = labelRenderedText.getBoundingBox();
		Point2D labelPosition = new Point2D.Double(bb.getCenterX(), bb.getMaxY());
		if (Geometry.crossProduct(firstDerivative, secondDerivative) < 0) {
			labelPosition.setLocation(labelPosition.getX(), bb.getMinY());
		}

		AffineTransform transform = AffineTransform.getTranslateInstance(
				middlePoint.getX() - labelPosition.getX(), middlePoint.getY() - labelPosition.getY());
		AffineTransform rotateTransform = AffineTransform.getRotateInstance(
				firstDerivative.getX(), firstDerivative.getY(), labelPosition.getX(), labelPosition.getY());
		transform.concatenate(rotateTransform);
		return transform;
	}

	protected void drawLabelInLocalSpace(DrawRequest r) {
		if (getLabelVisibility()) {
			cacheLabelRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();

			AffineTransform oldTransform = g.getTransform();
			AffineTransform transform = getLabelTransform();
			g.transform(transform);
			g.setColor(Coloriser.colorise(getLabelColor(), d.getColorisation()));
			labelRenderedText.draw(g);
			g.setTransform(oldTransform);
		}
	}

	@Override
	public void draw(DrawRequest r) {
		drawLabelInLocalSpace(r);
	}

	private Rectangle2D getLabelBoundingBox() {
		return BoundingBoxHelper.transform(labelRenderedText.getBoundingBox(), getLabelTransform());
	}

	@Override
	public Rectangle2D getBoundingBox() {
		Rectangle2D labelBB = getLabelBoundingBox();
		return BoundingBoxHelper.union(super.getBoundingBox(), labelBB);
	}

	@Override
	public boolean hitTest(Point2D pointInParentSpace) {
		Rectangle2D labelBB = getLabelBoundingBox();
		if (labelBB != null && labelBB.contains(pointInParentSpace)) return true;
		return super.hitTest(pointInParentSpace);
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color symbolColor) {
		this.labelColor = symbolColor;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL_COLOR));
	}

}
