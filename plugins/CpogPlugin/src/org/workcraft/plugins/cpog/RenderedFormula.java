package org.workcraft.plugins.cpog;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.formula.visitors.FormulaRenderingResult;
import org.workcraft.formula.visitors.FormulaToGraphics;
import org.workcraft.formula.visitors.StringGenerator;

public class RenderedFormula implements Touchable {
    public final String text;
    public final String formulaString;
    public final Font font;
    public final Positioning positioning;
    public final double xOffset;
    public final double yOffset;
    private final FormulaRenderingResult renderingResult;
    private final Rectangle2D boundingBox;

    public RenderedFormula(String text, BooleanFormula formula, Font font, Positioning positioning, Point2D offset) {
        this.text = text;
        this.formulaString = StringGenerator.toString(formula);
        this.font = font;
        this.positioning = positioning;
        this.xOffset = offset.getX();
        this.yOffset = offset.getY();
        final FontRenderContext context = new FontRenderContext(AffineTransform.getScaleInstance(1000.0, 1000.0), true, true);
        renderingResult = FormulaToGraphics.print(text, font, context);
        if (formula != One.getInstance()) {
            renderingResult.add(FormulaToGraphics.render(formula, context, font));
        }
        Rectangle2D bb = renderingResult.boundingBox;
        double x = offset.getX() + positioning.xOffset + 0.5 * positioning.xSign * bb.getWidth();
        double y = offset.getY() + positioning.yOffset + 0.5 * positioning.ySign * bb.getHeight();
        boundingBox = BoundingBoxHelper.move(bb, x - bb.getCenterX(), y - bb.getCenterY());
    }

    public boolean isDifferent(String text, BooleanFormula formula, Font font, Positioning positioning, Point2D offset) {
        if (text == null) {
            text = "";
        }
        String formulaString = StringGenerator.toString(formula);
        return !text.equals(this.text) || !formulaString.equals(this.formulaString) || !font.equals(this.font)
                || positioning != this.positioning || offset.getX() != this.xOffset || offset.getY() != this.yOffset;
    }

    public void draw(Graphics2D g) {
        g.setFont(font);
        AffineTransform oldTransform = g.getTransform();
        g.translate(boundingBox.getX(), boundingBox.getMaxY());
        renderingResult.draw(g);
        g.setTransform(oldTransform);
    }

    @Override
    public boolean hitTest(Point2D point) {
        return boundingBox.contains(point);
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    @Override
    public Point2D getCenter() {
        return new Point2D.Double(boundingBox.getCenterX(), boundingBox.getCenterY());
    }

    public boolean isEmpty() {
        return (text == null) || text.isEmpty();
    }

}
