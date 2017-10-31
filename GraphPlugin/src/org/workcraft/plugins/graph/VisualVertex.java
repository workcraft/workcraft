package org.workcraft.plugins.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;

@Hotkey(KeyEvent.VK_V)
@DisplayName("Vertex")
@SVGIcon("images/graph-node-vertex.svg")
public class VisualVertex extends VisualComponent {

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

    // Epsilon symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final char EPSILON_SYMBOL = 0x03B5;

    public static final String PROPERTY_RENDER_TYPE = "Render type";
    public static final String PROPERTY_SYMBOL_POSITIONING = "Symbol positioning";
    public static final String PROPERTY_SYMBOL_COLOR = "Symbol color";

    private RenderType renderType = RenderType.CIRCLE;

    public VisualVertex(Vertex vertex) {
        super(vertex);
        addPropertyDeclarations();
        removePropertyDeclarationByName(PROPERTY_LABEL);
        renamePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING, PROPERTY_SYMBOL_POSITIONING);
        renamePropertyDeclarationByName(PROPERTY_LABEL_COLOR, PROPERTY_SYMBOL_COLOR);
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
        g.setColor(Coloriser.colorise(getFillColor(), background));
        g.fill(shape);
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.setStroke(new BasicStroke((float) strokeWidth));
        g.draw(shape);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        if (getRenderType() == RenderType.LABEL) {
            return getLabelBoundingBox().contains(pointInLocalSpace);
        }
        return super.hitTestInLocalSpace(pointInLocalSpace);
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void setRenderType(RenderType value) {
        if (renderType != value) {
            renderType = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_RENDER_TYPE));
        }
    }

    @Override
    public Positioning getLabelPositioning() {
        if (getRenderType() == RenderType.LABEL) {
            return Positioning.CENTER;
        }
        return super.getLabelPositioning();
    }

    public Vertex getReferencedVertex() {
        return (Vertex) getReferencedComponent();
    }

    @Override
    public boolean getLabelVisibility() {
        return true;
    }

    @Override
    protected boolean cacheLabelRenderedText(DrawRequest r) {
        String label = Character.toString(EPSILON_SYMBOL);
        Symbol symbol = getReferencedVertex().getSymbol();
        if (symbol != null) {
            label = r.getModel().getMathName(symbol);
            return cacheLabelRenderedText(label, getLabelFont(), getLabelPositioning(), getLabelOffset());
        }
        return false;
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
