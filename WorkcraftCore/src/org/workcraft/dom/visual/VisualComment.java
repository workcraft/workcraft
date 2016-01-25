package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonCommentSettings;

@Hotkey(KeyEvent.VK_N)
@DisplayName("Text Note")
@SVGIcon("images/icons/svg/note.svg")
public class VisualComment extends VisualComponent implements Container {
	public static final String PROPERTY_TEXT_ALIGNMENT = "Text alignment";

	protected double size = CommonCommentSettings.getBaseSize();
	protected double strokeWidth = CommonCommentSettings.getStrokeWidth();
	protected Alignment textAlignment = CommonCommentSettings.getTextAlignment();

	public VisualComment(CommentNode note) {
		super(note);
		setLabelPositioning(Positioning.CENTER);
		setForegroundColor(CommonCommentSettings.getBorderColor());
		setFillColor(CommonCommentSettings.getFillColor());
		setLabelColor(CommonCommentSettings.getTextColor());
		removePropertyDeclarationByName("Label positioning");
		removePropertyDeclarationByName("Name color");
		removePropertyDeclarationByName("Name positioning");
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualComment, Alignment>(
				this, PROPERTY_TEXT_ALIGNMENT, Alignment.class, true, true, true) {
			protected void setter(VisualComment object, Alignment value) {
				object.setTextAlignment(value);
			}
			protected Alignment getter(VisualComment object) {
				return object.getTextAlignment();
			}
		});
	}

	public Alignment getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(Alignment value) {
		if (value != textAlignment) {
			textAlignment = value;
			sendNotification(new PropertyChangedEvent(this, PROPERTY_TEXT_ALIGNMENT));
		}
	}

	@Override
	public boolean getLabelVisibility() {
		return true;
	}

	@Override
	public Point2D getLabelOffset() {
		return new Point2D.Double(0.0, 0.0);
	}

	@Override
	public boolean getNameVisibility() {
		return false;
	}

	@Override
	public Point2D getNameOffset() {
		return new Point2D.Double(0.0, 0.0);
	}

	@Override
	public Alignment getLabelAlignment() {
        return getTextAlignment();
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		cacheRenderedText(r); // needed to better estimate the bounding box
		Rectangle2D shape = getBoundingBoxInLocalSpace();
		//shape.setRect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
		shape.setRect(shape.getX() - 0.1, shape.getY() - 0.1, shape.getWidth() + 0.2, shape.getHeight() + 0.2);
		g.setColor(Coloriser.colorise(getFillColor(), r.getDecoration().getBackground()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
		float w = (float)strokeWidth;
		float[] pattern = {10.0f * w, 10.0f * w};
		g.setStroke(new BasicStroke(w, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
		g.draw(shape);
		drawLabelInLocalSpace(r);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return BoundingBoxHelper.expand(super.getBoundingBoxInLocalSpace(), 0.2, 0.0);
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	@Override
	public void add(Node node) {
	}

	@Override
	public void add(Collection<Node> nodes) {
	}

	@Override
	public void remove(Node node) {
	}

	@Override
	public void remove(Collection<Node> nodes) {
	}

	@Override
	public void reparent(Collection<Node> nodes) {
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
	}

}
