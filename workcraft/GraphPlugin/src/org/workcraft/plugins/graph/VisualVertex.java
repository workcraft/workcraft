package org.workcraft.plugins.graph;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
    public static final String EPSILON_SYMBOL = Character.toString((char) 0x03B5);

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
        addPropertyDeclaration(new PropertyDeclaration<>(RenderType.class, PROPERTY_RENDER_TYPE,
                this::setRenderType, this::getRenderType).setCombinable().setTemplatable());
    }

    @Override
    public Vertex getReferencedComponent() {
        return (Vertex) super.getReferencedComponent();
    }

    @Override
    public Shape getShape() {
        double size = VisualCommonSettings.getNodeSize() - VisualCommonSettings.getStrokeWidth();
        double pos = -0.5 * size;
        Shape shape = new Ellipse2D.Double(pos, pos, size, size);
        if (getRenderType() != null) {
            switch (getRenderType()) {
                case SQUARE -> shape = new Rectangle2D.Double(pos, pos, size, size);
                case LABEL -> shape = new Path2D.Double();
                default -> shape = new Ellipse2D.Double(pos, pos, size, size);
            }
        }
        return shape;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        if (getRenderType() == RenderType.LABEL) {
            return getLabelBoundingBox().contains(pointInLocalSpace);
        }
        return getShape().contains(pointInLocalSpace);
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

    @Override
    public boolean getLabelVisibility() {
        return true;
    }

    @Override
    protected void cacheLabelRenderedText(DrawRequest r) {
        Symbol symbol = getReferencedComponent().getSymbol();
        if (symbol != null) {
            String label = r.getModel().getMathName(symbol);
            cacheLabelRenderedText(label, getLabelFont(), getLabelPositioning(), getLabelOffset());
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualVertex srcComponent) {
            setRenderType(srcComponent.getRenderType());
        }
    }

}
