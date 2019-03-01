package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.FormulaRenderingResult;
import org.workcraft.formula.utils.FormulaToGraphics;
import org.workcraft.utils.Coloriser;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanReplacer;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.utils.Geometry;

public class VisualArc extends VisualConnection {

    public static final String PROPERTY_CONDITION = "condition";
    private static Font labelFont;
    private Rectangle2D labelBB = null;

    Arc mathConnection;

    static {
        try {
            labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb")).deriveFont(0.5f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public VisualArc(Arc mathConnection) {
        super();
        this.mathConnection = mathConnection;
    }

    public VisualArc(Arc mathConnection, VisualVertex first, VisualVertex second) {
        super(mathConnection, first, second);
        this.mathConnection = mathConnection;
    }

    @NoAutoSerialisation
    public BooleanFormula getCondition() {
        return mathConnection.getCondition();
    }

    @NoAutoSerialisation
    public void setCondition(BooleanFormula condition) {
        mathConnection.setCondition(condition);
    }

    @Override
    public Stroke getStroke() {
        BooleanFormula value = evaluate();

        if (value == Zero.instance()) {
            return new BasicStroke((float) super.getLineWidth(), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1.0f, new float[] {0.18f, 0.18f}, 0.00f);
        }

        return super.getStroke();
    }

// FIXME: Gray colour of arcs with undecided conditions is confusing.
//    @Override
//    public Color getDrawColor() {
//        BooleanFormula value = evaluate();
//        if ((value != Zero.instance() && (value != One.instance())) {
//            return Color.LIGHT_GRAY;
//        }
//        return super.getDrawColor();
//    }

    private BooleanFormula evaluate() {
        BooleanFormula condition = getCondition();

        condition = BooleanOperations.and(condition, ((VisualVertex) getFirst()).evaluate());
        condition = BooleanOperations.and(condition, ((VisualVertex) getSecond()).evaluate());

        return condition.accept(new PrettifyBooleanReplacer());
    }

    @Override
    public void draw(DrawRequest r) {
        labelBB = null;

        if (getCondition() == One.instance()) return;

        Graphics2D g = r.getGraphics();

        FormulaRenderingResult result = FormulaToGraphics.render(getCondition(), g.getFontRenderContext(), labelFont);

        labelBB = result.boundingBox;

        ConnectionGraphic graphic = getGraphic();

        Point2D p = graphic.getPointOnCurve(0.5);
        Point2D d = graphic.getDerivativeAt(0.5);
        Point2D dd = graphic.getSecondDerivativeAt(0.5);

        if (d.getX() < 0) {
            d = Geometry.multiply(d, -1);
            //dd = Geometry.multiply(dd, -1);
        }

        Point2D labelPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getMaxY());
        if (Geometry.crossProduct(d, dd) < 0) labelPosition.setLocation(labelPosition.getX(), labelBB.getMinY());

        AffineTransform oldTransform = g.getTransform();
        AffineTransform transform = AffineTransform.getTranslateInstance(p.getX() - labelPosition.getX(), p.getY() - labelPosition.getY());
        transform.concatenate(AffineTransform.getRotateInstance(d.getX(), d.getY(), labelPosition.getX(), labelPosition.getY()));

        g.transform(transform);
        g.setColor(Coloriser.colorise(Color.BLACK, r.getDecoration().getColorisation()));
        result.draw(g);
        g.setTransform(oldTransform);

        labelBB = BoundingBoxHelper.transform(labelBB, transform);
    }

    @Override
    public Rectangle2D getBoundingBox() {
        return BoundingBoxHelper.union(super.getBoundingBox(), labelBB);
    }

    public Rectangle2D getLabelBoundingBox() {
        return labelBB;
    }

    @Override
    public boolean hitTest(Point2D pointInParentSpace) {
        if (labelBB != null && labelBB.contains(pointInParentSpace)) return true;

        return super.hitTest(pointInParentSpace);
    }

}
