package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.formula.CpogFormulaVariable;
import org.workcraft.plugins.cpog.formula.CpogVisitor;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanReplacer;

@Hotkey(KeyEvent.VK_V)
@DisplayName("Vertex")
@SVGIcon("images/cpog-node-vertex.svg")
public class VisualVertex extends VisualComponent implements CpogFormulaVariable {

    public enum RenderType {
        CIRCLE("Circle"),
        SQUARE("Square"),
        LABEL("Label");

        private final String name;

        RenderType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final String PROPERTY_CONDITION = "Condition";
    public static final String PROPERTY_RENDER_TYPE = "Render type";
    public static Font conditionFont;
    private RenderedFormula conditionRenderedFormula = new RenderedFormula("", One.instance(), conditionFont, getLabelPositioning(), getLabelOffset());
    private RenderType renderType = RenderType.CIRCLE;

    static {
        try {
            Font font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb"));
            conditionFont = font.deriveFont(0.5f);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VisualVertex(Vertex vertex) {
        super(vertex);
        addPropertyDeclarations();
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualVertex, RenderType>(
                this, PROPERTY_RENDER_TYPE, RenderType.class, true, true, true) {
            protected void setter(VisualVertex object, RenderType value) {
                object.setRenderType(value);
            }

            protected RenderType getter(VisualVertex object) {
                return object.getRenderType();
            }
        });
    }

    public Shape getShape() {
        double xy = -size / 2 + strokeWidth / 2;
        double wh = size - strokeWidth;
        Shape shape = new Ellipse2D.Double(xy, xy, wh, wh);
        if (getRenderType() != null) {
            switch (getRenderType()) {
            case CIRCLE:
                shape = new Ellipse2D.Double(xy, xy, wh, wh);
                break;
            case SQUARE:
                shape = new Rectangle2D.Double(xy, xy, wh, wh);
                break;
            case LABEL:
                shape = new Path2D.Double();
                break;
            default:
                shape = new Ellipse2D.Double(xy, xy, wh, wh);
                break;
            }
        }
        return shape;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Color background = r.getDecoration().getBackground();
        Shape shape = getShape();
        BooleanFormula value = evaluate();
        g.setColor(Coloriser.colorise(getFillColor(), background));
        g.fill(shape);
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        if (value == Zero.instance()) {
            g.setStroke(new BasicStroke((float) strokeWidth, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 1.0f, new float[] {0.18f, 0.18f}, 0.00f));
        } else {
            g.setStroke(new BasicStroke((float) strokeWidth));
// FIXME: Gray colour of vertices with undecided conditions is confusing.
//            if (value != One.instance())
//                g.setColor(Coloriser.colorise(Color.LIGHT_GRAY, colorisation));
        }
        g.draw(shape);
        drawConditionInLocalSpace(r);
    }

    protected void cacheConditionRenderedFormula(DrawRequest r) {
        String text = getLabel();
        if (getCondition() != One.instance()) {
            text += ": ";
        }
        if (conditionRenderedFormula.isDifferent(text, getCondition(), conditionFont, getLabelPositioning(), getLabelOffset())) {
            conditionRenderedFormula = new RenderedFormula(text, getCondition(), conditionFont, getLabelPositioning(), getLabelOffset());
        }
    }

    protected void drawConditionInLocalSpace(DrawRequest r) {
        if (getLabelVisibility()) {
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            cacheConditionRenderedFormula(r);
            if ((conditionRenderedFormula != null) && !conditionRenderedFormula.isEmpty()) {
                g.setColor(Coloriser.colorise(getLabelColor(), d.getColorisation()));
                conditionRenderedFormula.draw(g);
            }
        }
    }

    public Rectangle2D getConditionBoundingBox() {
        if ((conditionRenderedFormula != null) && !conditionRenderedFormula.isEmpty()) {
            return conditionRenderedFormula.getBoundingBox();
        } else {
            return null;
        }
    }

    public Vertex getMathVertex() {
        return (Vertex) getReferencedComponent();
    }

    public BooleanFormula getCondition() {
        return getMathVertex().getCondition();
    }

    public void setCondition(BooleanFormula condition) {
        getMathVertex().setCondition(condition);
        sendNotification(new PropertyChangedEvent(this, PROPERTY_CONDITION));
    }

    public BooleanFormula evaluate() {
        return getCondition().accept(new PrettifyBooleanReplacer());
    }

    @Override
    public <T> T accept(CpogVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (getLabelVisibility() && (conditionRenderedFormula != null) && !conditionRenderedFormula.isEmpty()) {
            bb = BoundingBoxHelper.union(bb, conditionRenderedFormula.getBoundingBox());
        }
        return bb;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        Shape shape = getShape();
        if (getRenderType() == RenderType.LABEL) {
            cacheLabelRenderedText(getLabel(), getLabelFont(), getLabelPositioning(), getLabelOffset());
            shape = BoundingBoxHelper.union(getLabelBoundingBox(), getConditionBoundingBox());
        }
        return shape.contains(pointInLocalSpace);
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType renderType) {
        if (this.renderType != renderType) {
            this.renderType = renderType;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_RENDER_TYPE));
        }
    }

    @Override
    public boolean getLabelVisibility() {
        if (getRenderType() == RenderType.LABEL) {
            return true;
        }
        return super.getLabelVisibility();
    }

    @Override
    public Positioning getLabelPositioning() {
        if (getRenderType() == RenderType.LABEL) {
            return Positioning.CENTER;
        }
        return super.getLabelPositioning();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualVertex) {
            VisualVertex srcComponent = (VisualVertex) src;
            setRenderType(srcComponent.getRenderType());
        }
    }

}
