package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@Hotkey(KeyEvent.VK_N)
@DisplayName("Text Note")
@SVGIcon("images/icons/svg/note.svg")
public class VisualComment extends VisualComponent {

	public VisualComment(CommentNode note) {
		super(note);
		setLabelPositioning(Positioning.CENTER);
		setForegroundColor(Color.GRAY);
		modifyPropertyDeclarations();
	}

	private void modifyPropertyDeclarations() {
		for (PropertyDescriptor declaration: getDescriptors()) {
			if (declaration.getName() == "Label positioning") {
				removePropertyDeclaration(declaration);
				break;
			}
		}
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		updateGlyph(g);
		Rectangle2D shape = getBoundingBoxInLocalSpace();
		shape.setRect(shape.getX() - 0.1, shape.getY() - 0.1, shape.getWidth() + 0.2, shape.getHeight() + 0.2);
		g.setColor(Coloriser.colorise(getFillColor(), r.getDecoration().getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
		g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{0.1f, 0.1f}, 0.0f));
		g.draw(shape);
		drawLabelInLocalSpace(r);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return BoundingBoxHelper.expand(super.getBoundingBoxInLocalSpace(), 0.6, 0.0);
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

}
